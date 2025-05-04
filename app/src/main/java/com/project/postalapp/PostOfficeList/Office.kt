package com.project.postalapp.PostOfficeList

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

@Entity(tableName = "office")
data class Office(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var Office: String,
    var Taluka: String,
    var District: String,
    var State: String,
    var Latitude: String,
    var Longitude: String,
    val distance: Double? = null
) {

}