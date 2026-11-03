package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.CaregiverDao
import com.strathmore.CareConnect.data.local.dao.LocationDao
import com.strathmore.CareConnect.data.local.dao.NurseDao
import com.strathmore.CareConnect.data.local.dao.RoleDao
import com.strathmore.CareConnect.data.local.dao.UserDao
import com.strathmore.CareConnect.data.local.entities.Caregiver
import com.strathmore.CareConnect.data.local.entities.Location
import com.strathmore.CareConnect.data.local.entities.Nurse
import com.strathmore.CareConnect.data.local.entities.User
import java.security.MessageDigest
import java.security.SecureRandom

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object AccountDeactivated : LoginResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val caregiverDao: CaregiverDao,
    private val nurseDao: NurseDao,
    private val locationDao: LocationDao
) {
    /** Registers a new Caregiver account. Returns the new userId, or throws with a message on failure. */
    suspend fun registerCaregiver(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        relationship: String
    ): Long {
        val existing = userDao.getByEmail(email.trim().lowercase())
        if (existing != null) {
            throw IllegalStateException("An account with this email already exists")
        }

        val caregiverRole = roleDao.getByName("Caregiver")
            ?: throw IllegalStateException("Caregiver role not found — is the database seeded?")

        val salt = generateSalt()
        val user = User(
            roleId = caregiverRole.roleId,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim().lowercase(),
            password = hash(password, salt),
            salt = salt,
            phoneNumber = phoneNumber.trim()
        )
        val userId = userDao.insert(user)

        caregiverDao.insert(Caregiver(userId = userId, relationship = relationship.trim()))

        return userId
    }

    /** Registers a new Nurse account with a location. Returns the new nurseId. */
    suspend fun registerNurse(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        qualification: String,
        city: String,
        latitude: Double,
        longitude: Double
    ): Long {
        val existing = userDao.getByEmail(email.trim().lowercase())
        if (existing != null) {
            throw IllegalStateException("An account with this email already exists")
        }

        val nurseRole = roleDao.getByName("Nurse")
            ?: throw IllegalStateException("Nurse role not found — is the database seeded?")

        val salt = generateSalt()
        val user = User(
            roleId = nurseRole.roleId,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim().lowercase(),
            password = hash(password, salt),
            salt = salt,
            phoneNumber = phoneNumber.trim()
        )
        val userId = userDao.insert(user)

        val nurseId = nurseDao.insert(
            Nurse(
                userId = userId,
                qualification = qualification.trim()
            )
        )

        locationDao.insert(
            Location(
                nurseId = nurseId,
                city = city.trim(),
                latitude = latitude,
                longitude = longitude
            )
        )

        return nurseId
    }

    suspend fun getNurseId(userId: Long): Long? =
        nurseDao.getByUserId(userId)?.nurseId

    /** Verifies credentials and account status. */
    suspend fun login(email: String, password: String): LoginResult {
        val user = userDao.getByEmail(email.trim().lowercase()) ?: return LoginResult.InvalidCredentials
        val attemptedHash = hash(password, user.salt)
        if (attemptedHash != user.password) return LoginResult.InvalidCredentials
        if (!user.active) return LoginResult.AccountDeactivated
        return LoginResult.Success(user)
    }

    suspend fun getCaregiverId(userId: Long): Long? =
        caregiverDao.getByUserId(userId)?.caregiverId

    suspend fun getRoleName(roleId: Long): String? =
        roleDao.getById(roleId)?.name

    suspend fun getUserForCaregiver(caregiverId: Long): User? {
        val caregiver = caregiverDao.getById(caregiverId) ?: return null
        return userDao.getById(caregiver.userId)
    }

    /** Resets the password for the given email. Throws if no account exists with that email. */
    suspend fun resetPassword(email: String, newPassword: String) {
        val user = userDao.getByEmail(email.trim().lowercase())
            ?: throw IllegalStateException("No account found with this email")

        val newSalt = generateSalt()
        val rowsUpdated = userDao.updatePassword(user.email, hash(newPassword, newSalt), newSalt)
        if (rowsUpdated == 0) {
            throw IllegalStateException("Failed to update password")
        }
    }

    private fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hash(raw: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest((salt + raw).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}