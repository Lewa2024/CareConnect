package com.strathmore.CareConnect.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Role::class,
            parentColumns = ["roleId"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("roleId"), Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val roleId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,   // salted hash, never plaintext
    val salt: String,       // unique per user, generated at registration
    val phoneNumber: String,
    val active: Boolean = true, // false = deactivated by an Administrator

    // Email verification: a new account can't log in until this is true. The code
    // itself is never stored in plaintext — verificationCodeHash is hash(code, salt),
    // the same hashing scheme used for the password, reusing the per-user salt above.
    val emailVerified: Boolean = false,
    val verificationCodeHash: String? = null,
    val verificationCodeExpiresAt: Long? = null
)