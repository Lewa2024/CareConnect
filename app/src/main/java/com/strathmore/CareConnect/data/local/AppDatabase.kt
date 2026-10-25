package com.strathmore.CareConnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.strathmore.CareConnect.data.local.dao.CaregiverDao
import com.strathmore.CareConnect.data.local.dao.NotificationDao
import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.dao.RoleDao
import com.strathmore.CareConnect.data.local.dao.RuleThresholdDao
import com.strathmore.CareConnect.data.local.dao.SymptomDao
import com.strathmore.CareConnect.data.local.dao.UserDao
import com.strathmore.CareConnect.data.local.entities.Caregiver
import com.strathmore.CareConnect.data.local.entities.Notification
import com.strathmore.CareConnect.data.local.entities.Patient
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
        Notification::class
    ],
    version = 6,
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

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

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
            userDao: com.strathmore.CareConnect.data.local.dao.UserDao
        ) {
            val existing = userDao.getByEmail("admin@careconnect.com")
            if (existing != null) return

            val adminRole = roleDao.getByName("Administrator") ?: return

            // Default demo admin account. Password: Admin@123
            // Salted with the same SHA-256(salt + password) scheme as AuthRepository.hash().
            val salt = java.security.SecureRandom().let { rnd ->
                val bytes = ByteArray(16)
                rnd.nextBytes(bytes)
                bytes.joinToString("") { "%02x".format(it) }
            }
            val passwordHash = java.security.MessageDigest.getInstance("SHA-256")
                .digest((salt + "Admin@123").toByteArray())
                .joinToString("") { "%02x".format(it) }

            userDao.insert(
                com.strathmore.CareConnect.data.local.entities.User(
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