package com.strathmore.CareConnect.data.remote

data class DarajaAuthResponse(
    val access_token: String,
    val expires_in: String
)

data class StkPushRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String = "CustomerPayBillOnline",
    val Amount: String,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String = "https://example.com/mpesa/callback",
    val AccountReference: String,
    val TransactionDesc: String
)

data class StkPushResponse(
    val MerchantRequestID: String?,
    val CheckoutRequestID: String?,
    val ResponseCode: String?,
    val ResponseDescription: String?,
    val CustomerMessage: String?,
    val errorCode: String?,
    val errorMessage: String?
)

data class StkPushQueryRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val CheckoutRequestID: String
)

data class StkPushQueryResponse(
    val ResponseCode: String?,
    val ResponseDescription: String?,
    val MerchantRequestID: String?,
    val CheckoutRequestID: String?,
    val ResultCode: String?,
    val ResultDesc: String?,
    val errorCode: String?,
    val errorMessage: String?
)