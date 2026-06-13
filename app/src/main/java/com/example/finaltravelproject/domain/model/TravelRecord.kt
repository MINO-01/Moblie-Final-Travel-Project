package com.example.finaltravelproject.domain.model

data class TravelRecord(
    val no: Int = 0,
    val place: String,
    val startDate: String,
    val endDate: String,
    val memo: String?,
    val photoUri: String?
)