package com.strathmore.CareConnect.data.remote

import com.strathmore.CareConnect.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ResendEmailClient {
    private const val BASE_URL = "https://api.resend.com/"

    val api: EmailApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            // BODY would print the Authorization header (API key) and the code in Logcat.
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailApiService::class.java)
    }
}