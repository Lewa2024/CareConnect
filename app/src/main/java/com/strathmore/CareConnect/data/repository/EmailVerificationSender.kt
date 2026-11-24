package com.strathmore.CareConnect.data.repository

import com.strathmore.CareConnect.BuildConfig
import com.strathmore.CareConnect.data.remote.ResendEmailClient
import com.strathmore.CareConnect.data.remote.SendEmailRequest

/**
 * Sends the email-verification code via Resend's transactional email API. Kept separate
 * from AuthRepository so the DB-facing logic there doesn't need to know about HTTP/Retrofit
 * — AuthRepository just calls sendVerificationCode() and gets a Result back.
 */
class EmailVerificationSender {
    suspend fun sendVerificationCode(toEmail: String, firstName: String, code: String): Result<Unit> {
        return try {
            val response = ResendEmailClient.api.sendEmail(
                bearerToken = "Bearer ${BuildConfig.RESEND_API_KEY}",
                request = SendEmailRequest(
                    from = BuildConfig.RESEND_FROM_EMAIL,
                    to = listOf(toEmail),
                    subject = "Verify your CareConnect account",
                    html = """
                        <p>Hi $firstName,</p>
                        <p>Thanks for registering with CareConnect. Your verification code is:</p>
                        <h2 style="letter-spacing: 4px;">$code</h2>
                        <p>This code expires in 15 minutes. If you didn't request this, you can ignore this email.</p>
                    """.trimIndent()
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Email API returned ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}