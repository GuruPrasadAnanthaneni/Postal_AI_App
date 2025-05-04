package com.project.postalapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report")
data class UserReport(
    var userId: String,
    var issue: String,
    var reply: String,
    var userName: String,
    var userMobile: String,
    var postedDate:String,
    @PrimaryKey(autoGenerate = true) var id: Int? = null,

    ) {

}
