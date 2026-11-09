package com.strathmore.CareConnect.data.repository

import android.util.Base64
import com.strathmore.CareConnect.BuildConfig
import com.strathmore.CareConnect.data.remote.DarajaClient
import com.strathmore.CareConnect.data.remote.StkPushRequest
import com.strathmore.CareConnect.data.remote.StkPushQueryRequest
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class StkPushOutcome {
    data class Success(val checkoutRequestId: String) : StkPushOutcome()
    data class Failed(val message: String) : StkPushOutcome()
}

sealed class StkPushStatus {
    data object Pending : StkPushStatus()
    data object Paid : StkPushStatus()
    data class Failed(val message: String) : StkPushStatus()
    data object TimedOut : StkPushStatus()
}

class DarajaRepository {

    // Used only as a fallback/sandbox reference — real calls now use the number the caregiver enters.
    private val sandboxTestPhoneNumber = "254708374149"

    private var cachedToken: String? = null
    private var tokenExpiresAtMillis: Long = 0L

    /**
     * Normalizes a Kenyan phone number to the 2547XXXXXXXX / 2541XXXXXXXX format
     * Safaricom's API requires, accepting 07..., 01..., +254..., or 254... as input.
     * Returns null if the result doesn't look like a valid Kenyan MSISDN.
     */
    fun normalizePhoneNumber(input: String): String? {
        var digits = input.trim().replace(" ", "").replace("-", "")
        if (digits.startsWith("+")) digits = digits.substring(1)
        digits = when {
            digits.startsWith("254") -> digits
            digits.startsWith("0") -> "254" + digits.substring(1)
            digits.startsWith("7") || digits.startsWith("1") -> "254$digits"
            else -> digits
        }
        return if (digits.length == 12 && digits.startsWith("254")) digits else null
    }

    private suspend fun getAccessToken(): String? {
        val now = System.currentTimeMillis()
        val existing = cachedToken
        if (existing != null && now < tokenExpiresAtMillis) return existing

        val credentials = "${BuildConfig.MPESA_CONSUMER_KEY}:${BuildConfig.MPESA_CONSUMER_SECRET}"
        val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val response = DarajaClient.api.getAccessToken("Basic $encoded")
        if (!response.isSuccessful) return null

        val body = response.body() ?: return null
        cachedToken = body.access_token
        tokenExpiresAtMillis = now + (body.expires_in.toLongOrNull() ?: 3500L) * 1000L - 60_000L
        return body.access_token
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())

    private fun password(timestamp: String): String {
        val raw = "${BuildConfig.MPESA_SHORTCODE}${BuildConfig.MPESA_PASSKEY}$timestamp"
        return Base64.encodeToString(raw.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun initiateStkPush(amountKsh: Double, phoneNumber: String, accountReference: String): StkPushOutcome {
        val token = getAccessToken()
            ?: return StkPushOutcome.Failed("Could not authenticate with M-Pesa. Check your Consumer Key/Secret.")

        val ts = timestamp()
        val request = StkPushRequest(
            BusinessShortCode = BuildConfig.MPESA_SHORTCODE,
            Password = password(ts),
            Timestamp = ts,
            Amount = amountKsh.toInt().toString(),
            PartyA = phoneNumber,
            PartyB = BuildConfig.MPESA_SHORTCODE,
            PhoneNumber = phoneNumber,
            AccountReference = accountReference.take(12),
            TransactionDesc = "CareConnect visit payment"
        )

        val response = DarajaClient.api.stkPush("Bearer $token", request)
        if (!response.isSuccessful) {
            return StkPushOutcome.Failed("Request failed: ${response.code()}")
        }

        val body = response.body()
        val checkoutId = body?.CheckoutRequestID
        return if (checkoutId != null) {
            StkPushOutcome.Success(checkoutId)
        } else {
            StkPushOutcome.Failed(body?.errorMessage ?: body?.ResponseDescription ?: "Unknown error starting payment")
        }
    }

    suspend fun queryStkPushStatus(checkoutRequestId: String): StkPushStatus {
        val token = getAccessToken() ?: return StkPushStatus.Failed("Lost connection to M-Pesa")

        val ts = timestamp()
        val request = StkPushQueryRequest(
            BusinessShortCode = BuildConfig.MPESA_SHORTCODE,
            Password = password(ts),
            Timestamp = ts,
            CheckoutRequestID = checkoutRequestId
        )

        val response = DarajaClient.api.stkPushQuery("Bearer $token", request)
        if (!response.isSuccessful) return StkPushStatus.Pending

        val body = response.body() ?: return StkPushStatus.Pending
        return when (body.ResultCode) {
            "0" -> StkPushStatus.Paid
            null -> StkPushStatus.Pending
            else -> StkPushStatus.Failed(body.ResultDesc ?: "Payment was not completed")
        }
    }

    suspend fun pollUntilResolved(
        checkoutRequestId: String,
        maxAttempts: Int = 12,
        intervalMillis: Long = 5000L
    ): StkPushStatus {
        repeat(maxAttempts) {
            delay(intervalMillis)
            val status = queryStkPushStatus(checkoutRequestId)
            if (status !is StkPushStatus.Pending) return status
        }
        return StkPushStatus.TimedOut
    }
}