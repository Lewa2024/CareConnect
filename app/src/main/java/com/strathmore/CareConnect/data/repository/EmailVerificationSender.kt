package com.strathmore.CareConnect.data.repository

import android.util.Log
import com.strathmore.CareConnect.BuildConfig
import com.strathmore.CareConnect.data.remote.ResendEmailClient
import com.strathmore.CareConnect.data.remote.SendEmailRequest

/** Thrown when the email API answers with a non-2xx status, so callers can react to the code. */
class EmailSendException(val httpCode: Int, message: String) : Exception(message)

/**
 * Sends one-time codes via Resend's transactional email API. Used both for
 * registration verification and for password reset.
 */
class EmailVerificationSender {
    suspend fun sendVerificationCode(
        toEmail: String,
        firstName: String,
        code: String,
        isPasswordReset: Boolean = false
    ): Result<Unit> {
        val subject = if (isPasswordReset) "Reset your CareConnect password"
                      else "Verify your CareConnect account"
        val intro = if (isPasswordReset)
            "We received a request to reset your CareConnect password. Your reset code is:"
        else
            "Thanks for registering with CareConnect. Your verification code is:"

        return try {
            val response = ResendEmailClient.api.sendEmail(
                bearerToken = "Bearer ${BuildConfig.RESEND_API_KEY}",
                request = SendEmailRequest(
                    from = BuildConfig.RESEND_FROM_EMAIL,
                    to = listOf(toEmail),
                    subject = subject,
                    html = """
                        <p>Hi $firstName,</p>
                        <p>$intro</p>
                        <h2 style="letter-spacing: 4px;">$code</h2>
                        <p>This code expires in 15 minutes. If you didn't request this, you can ignore this email.</p>
                    """.trimIndent()
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val body = response.errorBody()?.string()
                Log.e("EmailSender", "Resend returned ${response.code()}: $body")
                Result.failure(EmailSendException(response.code(), "Email API returned ${response.code()}: $body"))
            }
        } catch (e: Exception) {
            Log.e("EmailSender", "Sending verification email failed", e)
            Result.failure(e)
        }
    }
}