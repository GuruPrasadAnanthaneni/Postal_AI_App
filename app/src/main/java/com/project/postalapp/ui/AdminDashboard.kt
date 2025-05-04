package com.project.postalapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.postalapp.databinding.ActivityAdminDashboardBinding
import com.project.postalapp.databinding.DialogAddAnnouncementsBinding
import com.project.postalapp.response.CommonResponse
import com.project.postalapp.response.RetrofitInstance
import com.project.postalapp.utils.SessionManager
import com.project.postalapp.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale


class AdminDashboard : AppCompatActivity() {
    private val bind by lazy { ActivityAdminDashboardBinding.inflate(layoutInflater) }
    private val shared by lazy { SessionManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        bind.logoutadmin.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Confirm") { _, _ ->
                    shared.clearLoginState()
                    finishAffinity()
                    startActivity(Intent(this@AdminDashboard, LoginActivity::class.java))
                }
                .setNegativeButton("Dismiss") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        bind.addStation.setOnClickListener {
            startActivity(Intent(this, AddPostOffice::class.java))
        }

        bind.viewReports.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        bind.viewStation.setOnClickListener {
            startActivity(Intent(this, ViewList::class.java))
        }

        bind.addpost.setOnClickListener {
            val view = DialogAddAnnouncementsBinding.inflate(layoutInflater)
            MaterialAlertDialogBuilder(this)
                .setView(view.root)
                .setTitle("Add Announcements")
                .setMessage("Kindly enter your url")
                .setCancelable(false)
                .setPositiveButton("Upload Announcement") { dialog, _ ->
                    val schemeName = view.schemeName.text.toString()
                    val url = view.Url.text.toString()
                    if (schemeName.isNotEmpty() && url.isNotEmpty()) {
                        CoroutineScope(IO).launch {
                            RetrofitInstance.instance.uploadAnnouncement(
                                aName = schemeName,
                                aUrl = url,
                                aDate = simpleDateFormat.format(System.currentTimeMillis())
                            )
                                .enqueue(object : Callback<CommonResponse?> {
                                    override fun onResponse(
                                        p0: Call<CommonResponse?>,
                                        p1: Response<CommonResponse?>,
                                    ) {
                                        val response = p1.body()!!
                                        if (!response.error) {
                                            showToast("Task updated")
                                            dialog.dismiss()
                                        } else {
                                            showToast(response.message)
                                        }
                                    }

                                    override fun onFailure(
                                        p0: Call<CommonResponse?>,
                                        p1: Throwable,
                                    ) {
                                        showToast("Network Error : ${p1.message!!}")
                                    }
                                })
                        }
                    } else {
                        showToast("Kindly Enter all the fields")
                    }
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()

                }.create().show()
        }


        bind.viewpost.setOnClickListener {
            startActivity(Intent(this, ViewAnnouncements::class.java))

        }


    }
}