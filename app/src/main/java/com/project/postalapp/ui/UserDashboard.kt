package com.project.postalapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project.postalapp.PostOfficeList.Office
import com.project.postalapp.PostOfficeList.PostOfficeViewmodel
import com.project.postalapp.PostOfficeList.SearchViewActivity
import com.project.postalapp.chatbot.ChatActivity
import com.project.postalapp.databinding.ActivityUserDashboardBinding
import com.project.postalapp.room.AppDatabase
import com.project.postalapp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class UserDashboard : AppCompatActivity() {
    private val bind by lazy { ActivityUserDashboardBinding.inflate(layoutInflater) }
    private val shared by lazy { SessionManager(this) }
    private val db by lazy { AppDatabase.getDatabase(this).reviewDao() }
    private val viewModel :PostOfficeViewmodel by viewModels()
    var userId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        userId = "${shared.getUserId()}"

        val originalUserName = shared.getUserName()!!
        val userName = if (originalUserName.isNotEmpty()) {
            (originalUserName[0].uppercaseChar()) + originalUserName.substring(1)
        } else {
            originalUserName
        }

        val coloredUserName = "<font color='#FF5722'>$userName</font>"
        bind.usetTxt.text = Html.fromHtml("Welcome<br>$coloredUserName")


        bind.Profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        bind.userReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        bind.viewpost.setOnClickListener {
            startActivity(Intent(this, ViewAnnouncements::class.java))
        }

        bind.chat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        bind.searchtext.setOnClickListener {
            startActivity(Intent(this, SearchViewActivity::class.java))
        }
        /*viewModel.getData()
        viewModel.list.observe(this@UserDashboard){ it: List<Office>? ->
           if (it!=null){
               CoroutineScope(IO).launch {
                   db.insertData(it)
               }

           }
        }*/






        bind.ViewStations.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }




}