package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.data.local.dao.CaregiverDao
import com.strathmore.CareConnect.data.local.dao.RoleDao
import com.strathmore.CareConnect.data.local.dao.UserDao
import com.strathmore.CareConnect.data.local.entities.Caregiver
import com.strathmore.CareConnect.data.local.entities.User
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val caregiverDao: CaregiverDao
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

        val user = User(
            roleId = caregiverRole.roleId,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim().lowercase(),
            password = hash(password),
            phoneNumber = phoneNumber.trim()
        )
        val userId = userDao.insert(user)

        caregiverDao.insert(Caregiver(userId = userId, relationship = relationship.trim()))

        return userId
    }

    /** Returns the logged-in User, or null if the email/password don't match. */
    suspend fun login(email: String, password: String): User? {
        return userDao.login(email.trim().lowercase(), hash(password))
    }
     suspend fun getCaregiverId(userId: Long): Long? =
        caregiverDao.getByUserId(userId)?.caregiverId

    private fun hash(raw: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}