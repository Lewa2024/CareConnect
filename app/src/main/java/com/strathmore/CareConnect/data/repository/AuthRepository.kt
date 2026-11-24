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
    data class EmailNotVerified(val userId: Long, val email: String) : LoginResult()
}

/** Result of registerCaregiver()/registerNurse(): the account is created and a
 *  verification code has been (attempted to be) sent — the caller still needs to
 *  verify before the account can log in. */
data class RegistrationResult(
    val userId: Long,
    val email: String,
    val firstName: String
)

sealed class VerifyEmailResult {
    data class Success(val user: User) : VerifyEmailResult()
    data object InvalidOrExpiredCode : VerifyEmailResult()
    data object AccountNotFound : VerifyEmailResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val caregiverDao: CaregiverDao,
    private val nurseDao: NurseDao,
    private val locationDao: LocationDao,
    private val emailSender: EmailVerificationSender
) {
    companion object {
        private const val VERIFICATION_CODE_VALIDITY_MILLIS = 15L * 60 * 1000 // 15 minutes
    }

    /** Registers a new Caregiver account, then generates and emails a verification code. */
    suspend fun registerCaregiver(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        relationship: String
    ): RegistrationResult {
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

        sendVerificationCodeFor(userId, user.firstName, user.email, salt)

        return RegistrationResult(userId, user.email, user.firstName)
    }

    /** Registers a new Nurse account with a location, then generates and emails a verification code. */
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
    ): RegistrationResult {
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

        sendVerificationCodeFor(userId, user.firstName, user.email, salt)

        return RegistrationResult(userId, user.email, user.firstName)
    }

    suspend fun getNurseId(userId: Long): Long? =
        nurseDao.getByUserId(userId)?.nurseId

    /** Verifies credentials and account status, including whether the email is verified yet. */
    suspend fun login(email: String, password: String): LoginResult {
        val user = userDao.getByEmail(email.trim().lowercase()) ?: return LoginResult.InvalidCredentials
        val attemptedHash = hash(password, user.salt)
        if (attemptedHash != user.password) return LoginResult.InvalidCredentials
        if (!user.active) return LoginResult.AccountDeactivated
        if (!user.emailVerified) return LoginResult.EmailNotVerified(user.userId, user.email)
        return LoginResult.Success(user)
    }

    /** Checks a submitted verification code against the stored hash and expiry. */
    suspend fun verifyEmail(userId: Long, code: String): VerifyEmailResult {
        val user = userDao.getById(userId) ?: return VerifyEmailResult.AccountNotFound

        val expectedHash = user.verificationCodeHash
        val expiresAt = user.verificationCodeExpiresAt
        val isValid = expectedHash != null &&
            expiresAt != null &&
            System.currentTimeMillis() <= expiresAt &&
            hash(code.trim(), user.salt) == expectedHash

        if (!isValid) return VerifyEmailResult.InvalidOrExpiredCode

        userDao.markEmailVerified(userId)
        return VerifyEmailResult.Success(
            user.copy(emailVerified = true, verificationCodeHash = null, verificationCodeExpiresAt = null)
        )
    }

    /** Generates a fresh code and re-sends it, e.g. if the first email didn't arrive or expired. */
    suspend fun resendVerificationCode(userId: Long): Result<Unit> {
        val user = userDao.getById(userId)
            ?: return Result.failure(IllegalStateException("Account not found"))
        return sendVerificationCodeFor(userId, user.firstName, user.email, user.salt)
    }

    private suspend fun sendVerificationCodeFor(userId: Long, firstName: String, email: String, salt: String): Result<Unit> {
        val code = generateVerificationCode()
        val expiresAt = System.currentTimeMillis() + VERIFICATION_CODE_VALIDITY_MILLIS
        userDao.setVerificationCode(userId, hash(code, salt), expiresAt)
        return emailSender.sendVerificationCode(email, firstName, code)
    }

    private fun generateVerificationCode(): String =
        (100000..999999).random().toString()

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