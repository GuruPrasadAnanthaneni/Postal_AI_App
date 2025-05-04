package com.project.postalapp.chatbot

data class PincodeResponse(
    val Message: String,
    val Status: String,
    val PostOffice: List<PostOffice>?
)

data class PostOffice(
    val Name: String,
    val Description: String?,
    val BranchType: String,
    val DeliveryStatus: String,
    val Circle: String,
    val District: String,
    val Division: String,
    val Region: String,
    val State: String,
    val Country: String,
    val Pincode: String
)