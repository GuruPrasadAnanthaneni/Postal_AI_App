package com.project.postalapp.response

import com.project.postalapp.PostOfficeList.Office
import com.project.postalapp.model.Announcements
import com.project.postalapp.model.Entries
import com.project.postalapp.model.RailLocator


data class LoginResponse(
    var error: Boolean,
    var message: String,
    var data: ArrayList<Entries>,
    var data2: ArrayList<RailLocator>,
    var data3: ArrayList<Announcements>,
    var data4: ArrayList<Office>
)
