package com.strathmore.CareConnect.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

data class SendEmailRequest(
    val from: String,
    val to: List<String>,
    val subject: String,
    val html: String
)

data class SendEmailResponse(
    val id: String?
)

interface EmailApiService {
    @Headers("Content-Type: application/json")
    @POST("emails")
    suspend fun sendEmail(
        @Header("Authorization") bearerToken: String,
        @Body request: SendEmailRequest
    ): Response<SendEmailResponse>
}