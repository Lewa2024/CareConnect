package com.strathmore.CareConnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.strathmore.CareConnect.data.local.dao.BookingDao
import com.strathmore.CareConnect.data.local.dao.CaregiverDao
import com.strathmore.CareConnect.data.local.dao.LocationDao
import com.strathmore.CareConnect.data.local.dao.MedicationDao
import com.strathmore.CareConnect.data.local.dao.MedicationTimeDao
import com.strathmore.CareConnect.data.local.dao.NotificationDao
import com.strathmore.CareConnect.data.local.dao.NurseDao
import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.dao.PostVisitNoteDao
import com.strathmore.CareConnect.data.local.dao.RoleDao
import com.strathmore.CareConnect.data.local.dao.RuleThresholdDao
import com.strathmore.CareConnect.data.local.dao.SymptomDao
import com.strathmore.CareConnect.data.local.dao.UserDao
import com.strathmore.CareConnect.data.local.entities.Booking
import com.strathmore.CareConnect.data.local.entities.Caregiver
import com.strathmore.CareConnect.data.local.entities.Location
import com.strathmore.CareConnect.data.local.entities.Medication
import com.strathmore.CareConnect.data.local.entities.MedicationTime
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.data.local.entities.Nurse
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.local.entities.PostVisitNote
import com.strathmore.CareConnect.data.local.entities.Role
import com.strathmore.CareConnect.data.local.entities.RuleThreshold
import com.strathmore.CareConnect.data.local.entities.Symptom
import com.strathmore.CareConnect.data.local.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Role::class,
        User::class,
        Caregiver::class,
        Patient::class,
        Symptom::class,
        RuleThreshold::class,
        Notification::class,
        Medication::class,
        MedicationTime::class,
        Nurse::class,
        Location::class,
        Booking::class,
        PostVisitNote::class
    ],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun roleDao(): RoleDao
    abstract fun userDao(): UserDao
    abstract fun caregiverDao(): CaregiverDao
    abstract fun patientDao(): PatientDao
    abstract fun symptomDao(): SymptomDao
    abstract fun ruleThresholdDao(): RuleThresholdDao
    abstract fun notificationDao(): NotificationDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationTimeDao(): MedicationTimeDao
    abstract fun nurseDao(): NurseDao
    abstract fun locationDao(): LocationDao
    abstract fun bookingDao(): BookingDao
    abstract fun postVisitNoteDao(): PostVisitNoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patients ADD COLUMN baselineWeightKg REAL")
                db.execSQL("ALTER TABLE patients ADD COLUMN baselineSystolicBp INTEGER")
                db.execSQL("ALTER TABLE patients ADD COLUMN baselineDiastolicBp INTEGER")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS medications (
                        medicationId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patientId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        dosage TEXT NOT NULL,
                        timesPerDay INTEGER NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        active INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medications_patientId ON medications(patientId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS medication_times (
                        medicationTimeId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medicationId INTEGER NOT NULL,
                        hour INTEGER NOT NULL,
                        minute INTEGER NOT NULL,
                        FOREIGN KEY(medicationId) REFERENCES medications(medicationId) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_times_medicationId ON medication_times(medicationId)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN active INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE notifications ADD COLUMN ruleNumber INTEGER")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS nurses (
                        nurseId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        qualification TEXT NOT NULL,
                        available INTEGER NOT NULL DEFAULT 1,
                        verified INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(userId) REFERENCES users(userId) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_nurses_userId ON nurses(userId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS locations (
                        locationId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nurseId INTEGER NOT NULL,
                        city TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        FOREIGN KEY(nurseId) REFERENCES nurses(nurseId) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_locations_nurseId ON locations(nurseId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        bookingId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patientId INTEGER NOT NULL,
                        nurseId INTEGER NOT NULL,
                        requestedAt INTEGER NOT NULL,
                        requestedVisitTime INTEGER NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE,
                        FOREIGN KEY(nurseId) REFERENCES nurses(nurseId) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bookings_patientId ON bookings(patientId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bookings_nurseId ON bookings(nurseId)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS post_visit_notes (
                        noteId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        bookingId INTEGER NOT NULL,
                        patientId INTEGER NOT NULL,
                        nurseId INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE,
                        FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE,
                        FOREIGN KEY(nurseId) REFERENCES nurses(nurseId) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_post_visit_notes_bookingId ON post_visit_notes(bookingId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_post_visit_notes_patientId ON post_visit_notes(patientId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_post_visit_notes_nurseId ON post_visit_notes(nurseId)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE patients_new (
                        patientId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        caregiverId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        dateOfBirth INTEGER NOT NULL,
                        sex TEXT NOT NULL DEFAULT '',
                        chiefComplaint TEXT NOT NULL DEFAULT '',
                        historyOfIllness TEXT NOT NULL DEFAULT '',
                        pastMedicalHistory TEXT NOT NULL DEFAULT '',
                        surgicalHistory TEXT NOT NULL DEFAULT '',
                        familyHistory TEXT NOT NULL DEFAULT '',
                        currentPlan TEXT NOT NULL DEFAULT '',
                        heightCm REAL,
                        baselineWeightKg REAL,
                        baselineSystolicBp INTEGER,
                        baselineDiastolicBp INTEGER,
                        FOREIGN KEY(caregiverId) REFERENCES caregivers(caregiverId) ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO patients_new (
                        patientId, caregiverId, name, dateOfBirth, pastMedicalHistory,
                        heightCm, baselineWeightKg, baselineSystolicBp, baselineDiastolicBp
                    )
                    SELECT patientId, caregiverId, name, dateOfBirth, medicalHistory,
                           heightCm, baselineWeightKg, baselineSystolicBp, baselineDiastolicBp
                    FROM patients
                """.trimIndent())

                db.execSQL("DROP TABLE patients")
                db.execSQL("ALTER TABLE patients_new RENAME TO patients")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_patients_caregiverId ON patients(caregiverId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            val existing = INSTANCE
            if (existing != null) return existing

            synchronized(this) {
                val existingInSync = INSTANCE
                if (existingInSync != null) return existingInSync

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "care_connect.db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    seedRolesIfMissing(instance.roleDao())
                    seedDefaultThresholdIfMissing(instance.ruleThresholdDao())
                    seedDefaultAdminIfMissing(instance.roleDao(), instance.userDao())
                }

                return instance
            }
        }

        private suspend fun seedDefaultThresholdIfMissing(ruleThresholdDao: RuleThresholdDao) {
            if (ruleThresholdDao.getByMetric("Pain") == null) {
                ruleThresholdDao.insert(
                    RuleThreshold(
                        metric = "Pain",
                        thresholdValue = 8,
                        consecutiveEntriesRequired = 3
                    )
                )
            }
        }

        private suspend fun seedDefaultAdminIfMissing(
            roleDao: RoleDao,
            userDao: UserDao
        ) {
            val existing = userDao.getByEmail("admin@careconnect.com")
            if (existing != null) return

            val adminRole = roleDao.getByName("Administrator") ?: return

            val salt = java.security.SecureRandom().let { rnd ->
                val bytes = ByteArray(16)
                rnd.nextBytes(bytes)
                bytes.joinToString("") { "%02x".format(it) }
            }
            val passwordHash = java.security.MessageDigest.getInstance("SHA-256")
                .digest((salt + "Admin@123").toByteArray())
                .joinToString("") { "%02x".format(it) }

            userDao.insert(
                User(
                    roleId = adminRole.roleId,
                    firstName = "System",
                    lastName = "Administrator",
                    email = "admin@careconnect.com",
                    password = passwordHash,
                    salt = salt,
                    phoneNumber = "0700000000"
                )
            )
        }

        private suspend fun seedRolesIfMissing(roleDao: RoleDao) {
            if (roleDao.getByName("Caregiver") == null) {
                roleDao.insert(Role(name = "Caregiver", description = "Family caregiver of a cancer patient"))
            }
            if (roleDao.getByName("Nurse") == null) {
                roleDao.insert(Role(name = "Nurse", description = "Registered nurse offering home visits"))
            }
            if (roleDao.getByName("Administrator") == null) {
                roleDao.insert(Role(name = "Administrator", description = "System administrator"))
            }
        }
    }
}