package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.NotificationDao
import com.strathmore.CareConnect.data.local.dao.RuleThresholdDao
import com.strathmore.CareConnect.data.local.dao.SymptomDao
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.data.local.entities.RuleThreshold
import com.strathmore.CareConnect.data.local.entities.Symptom

/**
 * Evaluates a newly logged symptom against four rules (see project proposal, Core
 * System Logic). Each rule is independent — more than one can fire from a single log
 * event, producing more than one Notification.
 *
 * "Baseline" for a symptom type = the average rating of the patient's prior active
 * entries of that type (excluding the entry currently being evaluated). A type with
 * no prior history has no baseline yet, so baseline-relative rules (2-4) are skipped
 * for it until enough history exists — a known boundary condition for new patients.
 */
class RuleEngine(
    private val symptomDao: SymptomDao,
    private val ruleThresholdDao: RuleThresholdDao,
    private val notificationDao: NotificationDao
) {
    companion object {
        private const val RAPID_ESCALATION_POINTS = 3
        private const val RAPID_ESCALATION_WINDOW_MILLIS = 24L * 60 * 60 * 1000
        private const val MULTI_SYMPTOM_MIN_TYPES = 2
        private const val PERSISTENCE_MIN_ENTRIES = 5
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

        checkRule1AbsoluteThreshold(patientId, symptomId, type)?.let { fired += it }
        checkRule2RapidEscalation(patientId, symptomId, type)?.let { fired += it }
        checkRule3MultiSymptom(patientId, symptomId)?.let { fired += it }
        checkRule4Persistence(patientId, symptomId, type)?.let { fired += it }

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

        return buildNotification(patientId, symptomId, message, urgency = "URGENT")
    }

    // Rule 2 — Rapid Escalation: any symptom rises >= 3 points above its baseline
    // within 24 hours (ROUTINE).
    private suspend fun checkRule2RapidEscalation(patientId: Long, symptomId: Long, type: String): Notification? {
        val allEntries = symptomDao.getAllByType(patientId, type) // newest first
        if (allEntries.size < 2) return null

        val current = allEntries.first()
        val prior = allEntries.drop(1)
        val baseline = prior.map { it.rating }.average()

        val rise = current.rating - baseline
        val withinWindow = (current.date - prior.first().date) <= RAPID_ESCALATION_WINDOW_MILLIS

        if (rise < RAPID_ESCALATION_POINTS || !withinWindow) return null

        val message = "Rule 2 (Rapid Escalation): $type rose from a baseline of ~${"%.1f".format(baseline)} " +
            "to ${current.rating}/10 within 24 hours."

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE")
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

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE")
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

        return buildNotification(patientId, symptomId, message, urgency = "ROUTINE")
    }

    private fun buildNotification(patientId: Long, symptomId: Long, message: String, urgency: String) =
        Notification(
            patientId = patientId,
            message = message,
            urgency = urgency,
            timestamp = System.currentTimeMillis(),
            triggeringSymptomId = symptomId
        )
}