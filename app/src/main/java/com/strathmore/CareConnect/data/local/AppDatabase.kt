package com.strathmore.CareConnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.strathmore.CareConnect.data.local.dao.CaregiverDao
import com.strathmore.CareConnect.data.local.dao.PatientDao
import com.strathmore.CareConnect.data.local.dao.RoleDao
import com.strathmore.CareConnect.data.local.dao.SymptomDao
import com.strathmore.CareConnect.data.local.dao.UserDao
import com.strathmore.CareConnect.data.local.entities.Caregiver
import com.strathmore.CareConnect.data.local.entities.Patient
import com.strathmore.CareConnect.data.local.entities.Role
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
        Symptom::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun roleDao(): RoleDao
    abstract fun userDao(): UserDao
    abstract fun caregiverDao(): CaregiverDao
    abstract fun patientDao(): PatientDao
    abstract fun symptomDao(): SymptomDao

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
                    // Offline-first: app must work fully without a network connection.
                    .fallbackToDestructiveMigration() // replace with real Migrations before release
                    .build()

                INSTANCE = instance

                // Self-healing seed: runs on every launch (not just first creation), so
                // a destructive schema migration that wipes the roles table gets fixed
                // automatically on the next app start, instead of crashing registration.
                CoroutineScope(Dispatchers.IO).launch {
                    seedRolesIfMissing(instance.roleDao())
                }

                return instance
            }
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