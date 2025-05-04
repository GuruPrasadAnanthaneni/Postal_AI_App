package com.project.postalapp.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    val userId: String,
    val rating: Float,
    val comment: String?,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)
