package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.NotificationDao
import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.dao.RuleThresholdDao
import com.strathmore.CareConnect.data.local.dao.SymptomDao
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.data.local.entities.RuleThreshold
import com.strathmore.CareConnect.data.local.entities.Symptom

/**
 * Evaluates a newly logged symptom against seven rules: four established early in the
 * project (Rules 1-4, working from pain/energy/appetite entries) and three added once
 * the vitals check-in fields existed on Symptom (Rules 5-7, covering same-session
 * multi-symptom entry, medication adherence, and weight). Each rule is independent —
 * more than one can fire from a single log event, producing more than one Notification.
 *
 * "Baseline" for a symptom type = the average rating of the patient's prior active
 * entries of that type (excluding the entry currently being evaluated). A type with
 * no prior history has no baseline yet, so baseline-relative rules (2-4) are skipped
 * for it until enough history exists — a known boundary condition for new patients.
 * Rules 6 and 7 do not use this rolling baseline at all: Rule 6 looks at whether
 * medication was marked taken, and Rule 7 compares against the patient's fixed
 * baselineWeightKg rather than a rolling average.
 *
 * Repeat-alert suppression: several rules (3, 6, and to a lesser extent 1, 2, 4) check
 * a condition that can still be true on the very next log, even though nothing new has
 * happened since the last alert — e.g. Rule 3 keeps seeing "2+ symptoms above baseline"
 * on every subsequent entry until one of those symptoms actually drops back down. Rather
 * than raising a fresh notification every single time, a rule's result is suppressed if
 * that same rule also fired on the entry immediately before this one (the most recent
 * prior entry for this patient, of any type). If a different entry was logged in between
 * where the rule did NOT fire, the next firing is treated as new and shown again.
 */
class RuleEngine(
    private val symptomDao: SymptomDao,
    private val ruleThresholdDao: RuleThresholdDao,
    private val notificationDao: NotificationDao,
    private val patientDao: PatientDao
) {
    companion object {
        private const val RAPID_ESCALATION_POINTS = 3       // a rise this big always counts, regardless of where it lands
        private const val RAPID_ESCALATION_POINTS_MINOR = 2 // a smaller rise still counts if it lands high enough
        private const val RAPID_ESCALATION_HIGH_FLOOR = 7   // "high enough" for the smaller-rise case
        private const val RAPID_ESCALATION_WINDOW_MILLIS = 24L * 60 * 60 * 1000
        private const val MULTI_SYMPTOM_MIN_TYPES = 2
        private const val PERSISTENCE_MIN_ENTRIES = 5

        // Rule 5 — two or more symptoms logged in the same submission count only if
        // each one is at least this severe on its own.
        private const val SIMULTANEOUS_ENTRY_SEVERITY_FLOOR = 6
        private const val SIMULTANEOUS_ENTRY_MIN_TYPES = 2

        // Rule 6 — this many most-recent ANSWERED medication check-ins, all missed,
        // in a row.
        private const val MEDICATION_MISSED_STREAK = 3

        // Rule 7 — a drop of this many percent or more from baselineWeightKg.
        private const val WEIGHT_LOSS_PERCENT_FLOOR = 5.0
    }
    suspend fun ensureDefaultThresholdsSeeded() {
        if (ruleThresholdDao.getByMetric("Pain") == null) {
            ruleThresholdDao.insert(
                RuleThreshold(metric = "Pain", thresholdValue = 8, consecutiveEntriesRequired = 3)
            )
        }
    }

    /** Call this right after a new symptom is logged. Returns every Notification that fired (may be empty). */
    suspend fun evaluateAfterLogging(patientId: Long, symptomId: Long, type: String): List<Notification> {
        val fired = mutableListOf<Notification>()

        // The entry immediately before this one (any type), used to suppress a rule
        // that already fired for the same still-true condition last time around.
        val currentEntry = symptomDao.getById(symptomId)
        val currentDate = currentEntry?.date ?: System.currentTimeMillis()
        val previousEntry = symptomDao.getPreviousEntryBeforeDate(patientId, currentDate)

        suspend fun addUnlessRepeatOfLastTime(ruleNumber: Int, notification: Notification?) {
            if (notification == null) return
            val firedLastTimeToo = previousEntry != null &&
                notificationDao.getNotificationForRuleAndSymptom(ruleNumber, previousEntry.symptomId) != null
            if (!firedLastTimeToo) {
                fired += notification
            }
        }

        addUnlessRepeatOfLastTime(1, checkRule1AbsoluteThreshold(patientId, symptomId, type))
        addUnlessRepeatOfLastTime(2, checkRule2RapidEscalation(patientId, symptomId, type))
        addUnlessRepeatOfLastTime(3, checkRule3MultiSymptom(patientId, symptomId))
        addUnlessRepeatOfLastTime(4, checkRule4Persistence(patientId, symptomId, type))
        addUnlessRepeatOfLastTime(5, checkRule5SimultaneousMultiSymptomEntry(patientId, symptomId))
        addUnlessRepeatOfLastTime(6, checkRule6MedicationNonAdherence(patientId, symptomId))
        addUnlessRepeatOfLastTime(7, checkRule7UnintentionalWeightLoss(patientId, symptomId))

        fired.forEach { notificationDao.insert(it) }
        return fired
    }

    // Rule 1 — Absolute Threshold: Pain >= 8 for 3 consecutive entries (URGENT).
    // Configurable per type via RuleThreshold; falls back to a safety-net rule
    // (severity >= 8, 1 entry) for any type without an explicit threshold configured.
    private suspend fun checkRule1AbsoluteThreshold(patientId: Long, symptomId: Long, type: String): Notification? {
        val threshold = ruleThresholdDao.getByMetric(type)
            ?: RuleThreshold(metric = type, thresholdValue = 8, consecutiveEntriesRequired = 1)

        val recentEntries = symptomDao.getRecentByType(patientId, type, threshold.consecutiveEntriesRequired)
        val streakMet = recentEntries.size >= threshold.consecutiveEntriesRequired &&
            recentEntries.all { it.rating >= threshold.thresholdValue }

        if (!streakMet) return null

        val message = if (threshold.consecutiveEntriesRequired > 1) {
            "Rule 1 (Absolute Threshold): $type has been ${threshold.thresholdValue}/10 or higher for " +
                "${threshold.consecutiveEntriesRequired} consecutive entries."
        } else {
            "Rule 1 (Absolute Threshold): $type was logged at ${threshold.thresholdValue}/10 or higher."
        }

                return buildNotification(patientId, symptomId, message, urgency = "URGENT", ruleNumber = 1)
    }

        // Rule 2 — Rapid Escalation: fires when EITHER (a) a symptom rises >= 3 points
    // above its baseline, OR (b) it rises >= 2 points AND lands at a high absolute
    // severity (>= 7) — catching cases like a 6-to-8 jump that's a smaller rise but
    // still ends up seriously high. Either way, must happen within 24 hours (ROUTINE).
    private suspend fun checkRule2RapidEscalation(patientId: Long, symptomId: Long, type: String): Notification? {
        val allEntries = symptomDao.getAllByType(patientId, type) // newest first
        if (allEntries.size < 2) return null

        val current = allEntries.first()
        val prior = allEntries.drop(1)
        val baseline = prior.map { it.rating }.average()

        val rise = current.rating - baseline
        val withinWindow = (current.date - prior.first().date) <= RAPID_ESCALATION_WINDOW_MILLIS

        val bigJump = rise >= RAPID_ESCALATION_POINTS
        val smallerButHighJump = rise >= RAPID_ESCALATION_POINTS_MINOR && current.rating >= RAPID_ESCALATION_HIGH_FLOOR

        if ((!bigJump && !smallerButHighJump) || !withinWindow) return null

        val message = "Rule 2 (Rapid Escalation): $type rose from a typical level of ~${"%.1f".format(baseline)} " +
            "to ${current.rating}/10 within 24 hours."

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE", ruleNumber = 2)
    }

    // Rule 3 — Multi-Symptom: two or more symptom types are currently above their
    // own baselines at the same time (ROUTINE).
    private suspend fun checkRule3MultiSymptom(patientId: Long, symptomId: Long): Notification? {
        val types = symptomDao.getDistinctTypesForPatient(patientId)
        val typesAboveBaseline = mutableListOf<String>()

        for (t in types) {
            val entries = symptomDao.getAllByType(patientId, t) // newest first
            if (entries.size < 2) continue
            val latest = entries.first()
            val baseline = entries.drop(1).map { it.rating }.average()
            if (latest.rating > baseline) {
                typesAboveBaseline += t
            }
        }

        if (typesAboveBaseline.size < MULTI_SYMPTOM_MIN_TYPES) return null

        val message = "Rule 3 (Multi-Symptom): ${typesAboveBaseline.size} symptoms " +
            "(${typesAboveBaseline.joinToString(", ")}) are currently above their individual baselines."

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE", ruleNumber = 3)
    }

    // Rule 4 — Persistence: a symptom has stayed above its baseline for 5 or more
    // consecutive entries (ROUTINE).
    private suspend fun checkRule4Persistence(patientId: Long, symptomId: Long, type: String): Notification? {
        val allEntries = symptomDao.getAllByType(patientId, type) // newest first
        if (allEntries.size < PERSISTENCE_MIN_ENTRIES + 1) return null

        val recentWindow = allEntries.take(PERSISTENCE_MIN_ENTRIES)
        val olderHistory = allEntries.drop(PERSISTENCE_MIN_ENTRIES)
        val baseline = olderHistory.map { it.rating }.average()

        val allAboveBaseline = recentWindow.all { it.rating > baseline }
        if (!allAboveBaseline) return null

        val message = "Rule 4 (Persistence): $type has remained above its baseline of " +
            "~${"%.1f".format(baseline)} for the last $PERSISTENCE_MIN_ENTRIES entries."

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE", ruleNumber=4)
    }

    // Rule 5 — Simultaneous Multi-Symptom Entry: two or more symptom types were saved
    // in the exact same submission (same `date`), and each one is severe enough on
    // its own (>= SIMULTANEOUS_ENTRY_SEVERITY_FLOOR). Unlike Rule 3, this never looks
    // at history or a baseline — it reacts to what the caregiver just reported
    // together, which is itself a signal worth flagging even for a brand-new patient
    // with no baseline yet.
    private suspend fun checkRule5SimultaneousMultiSymptomEntry(patientId: Long, symptomId: Long): Notification? {
        val current = symptomDao.getById(symptomId) ?: return null
        val sameSubmission = symptomDao.getActiveByPatientAndDate(patientId, current.date)

        // evaluateAfterLogging runs once per inserted row, so a two-symptom submission
        // calls this twice (once per row) with an identical sameSubmission set. Only
        // let the last-inserted row in the batch actually raise the notification, so a
        // simultaneous entry fires once per submission rather than once per row in it.
        if (symptomId != sameSubmission.maxOf { it.symptomId }) return null

        val severeTypes = sameSubmission
            .filter { it.rating >= SIMULTANEOUS_ENTRY_SEVERITY_FLOOR }
            .map { it.type }
            .distinct()

        if (severeTypes.size < SIMULTANEOUS_ENTRY_MIN_TYPES) return null

        val message = "Rule 5 (Simultaneous Multi-Symptom Entry): ${severeTypes.size} symptoms " +
            "(${severeTypes.joinToString(", ")}) were logged together, each at $SIMULTANEOUS_ENTRY_SEVERITY_FLOOR/10 or higher."

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE", ruleNumber = 5)
    }

    // Rule 6 — Medication Non-Adherence: the most recent ANSWERED medication
    // check-ins (medicationTaken != null) are all "missed" (false), for
    // MEDICATION_MISSED_STREAK entries in a row. Entries where the caregiver didn't
    // answer the question at all are skipped rather than counted either way.
    private suspend fun checkRule6MedicationNonAdherence(patientId: Long, symptomId: Long): Notification? {
        val recentAnswers = symptomDao.getRecentMedicationAnswers(patientId, MEDICATION_MISSED_STREAK)
        if (recentAnswers.size < MEDICATION_MISSED_STREAK) return null

        val allMissed = recentAnswers.all { it.medicationTaken == false }
        if (!allMissed) return null

        val message = "Rule 6 (Medication Non-Adherence): medication was marked as not taken for the " +
            "last $MEDICATION_MISSED_STREAK check-ins that answered the question."

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE", ruleNumber = 6)
    }

    // Rule 7 — Unintentional Weight Loss: the most recent entry that included a
    // weight reading has dropped WEIGHT_LOSS_PERCENT_FLOOR percent or more below the
    // patient's fixed baselineWeightKg (captured at registration, not a rolling
    // average like Rules 2-4 use).
    private suspend fun checkRule7UnintentionalWeightLoss(patientId: Long, symptomId: Long): Notification? {
        val latestWeightEntry = symptomDao.getMostRecentWeightEntry(patientId) ?: return null
        val currentWeight = latestWeightEntry.weightKg ?: return null
        val baseline = patientDao.getById(patientId)?.baselineWeightKg ?: return null
        if (baseline <= 0.0) return null

        val percentDrop = ((baseline - currentWeight) / baseline) * 100.0
        if (percentDrop < WEIGHT_LOSS_PERCENT_FLOOR) return null

        val message = "Rule 7 (Unintentional Weight Loss): latest weight of ${"%.1f".format(currentWeight)}kg is " +
            "${"%.1f".format(percentDrop)}% below the patient's baseline of ${"%.1f".format(baseline)}kg."

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE", ruleNumber = 7)
    }

        private fun buildNotification(patientId: Long, symptomId: Long, message: String, urgency: String, ruleNumber: Int) =
        Notification(
            patientId = patientId,
            message = message,
            urgency = urgency,
            timestamp = System.currentTimeMillis(),
            triggeringSymptomId = symptomId,
            ruleNumber = ruleNumber
        )
}