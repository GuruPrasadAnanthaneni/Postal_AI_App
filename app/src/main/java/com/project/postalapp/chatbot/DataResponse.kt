package com.example.cropiq.model

data class DataResponse(
    val isUser: Int,
    val prompt: String,
    val imageUri: String,
)