package com.strathmore.CareConnect.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface DarajaApiService {

    @GET("oauth/v1/generate")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Query("grant_type") grantType: String = "client_credentials"
    ): Response<DarajaAuthResponse>

    @Headers("Content-Type: application/json")
    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun stkPush(
        @Header("Authorization") bearerToken: String,
        @Body request: StkPushRequest
    ): Response<StkPushResponse>

    @Headers("Content-Type: application/json")
    @POST("mpesa/stkpushquery/v1/query")
    suspend fun stkPushQuery(
        @Header("Authorization") bearerToken: String,
        @Body request: StkPushQueryRequest
    ): Response<StkPushQueryResponse>
}