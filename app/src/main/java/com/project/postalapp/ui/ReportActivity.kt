package com.project.postalapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.postalapp.adapter.UserReportAdapter
import com.project.postalapp.databinding.ActivityReportBinding
import com.project.postalapp.databinding.CustomalertlayoutBinding
import com.project.postalapp.model.UserReport
import com.project.postalapp.room.AppDatabase
import com.project.postalapp.utils.SessionManager
import com.project.postalapp.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportActivity : AppCompatActivity() {
    private val bind by lazy { ActivityReportBinding.inflate(layoutInflater) }
    private val shared by lazy { SessionManager(applicationContext) }
    private val db by lazy { AppDatabase.getDatabase(applicationContext) }
    private lateinit var reportAdapter: UserReportAdapter
    private var role = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        bind.report.isVisible = false

        role = shared.getUserRole()!!
        val id = shared.getUserId() ?: return

        setupRecyclerView()
        fetchReports(id)

        bind.report.setOnClickListener {
            showReportDialog()
        }

        if (role != "Admin") {
            bind.textView6.text = "My Reports"
            bind.report.isVisible = true

        } else {
            bind.textView6.text = "Reports"
            bind.report.isVisible = false

        }
    }

    private fun setupRecyclerView() {
        reportAdapter = UserReportAdapter(emptyList(), { updateReport(it) }, role)
        bind.rvlist.apply {
            adapter = reportAdapter
            layoutManager = LinearLayoutManager(this@ReportActivity)
        }
    }

    private fun fetchReports(userId: String) {
        db.reviewDao().getReports().observe(this) { reports ->
            val filteredReports = if (role == "Admin") {
                reports
            } else {
                reports.filter { it.userId == userId }
            }
            reportAdapter.updateData(filteredReports)
        }
    }

    private fun showReportDialog() {
        val alertBinding = CustomalertlayoutBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(this)
            .setView(alertBinding.root)
            .setTitle("Report your Issue Here")
            .setCancelable(false)
            .setPositiveButton("Submit") { dialog, _ ->
                val issueText = alertBinding.suggest.text.toString().trim()
                if (issueText.isNotEmpty()) {
                    val report = UserReport(
                        userId = shared.getUserId() ?: "",
                        issue = issueText,
                        reply = "",
                        userName = shared.getUserName() ?: "",
                        userMobile = shared.getUserMobile() ?: "",
                        postedDate = System.currentTimeMillis().toString()
                    )

                    // Insert Report in Background Thread
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.reviewDao().insertReport(report)
                    }
                    dialog.dismiss()
                } else {
                    showToast("Empty field")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateReport(report: UserReport) {
        val alertBinding = CustomalertlayoutBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(this)
            .setView(alertBinding.root)
            .setTitle("Reply")
            .setCancelable(false)
            .setPositiveButton("Submit") { dialog, _ ->
                val replyText = alertBinding.suggest.text.toString().trim()
                if (replyText.isNotEmpty()) {
                    // Update Report in Background Thread
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.reviewDao().updateReport(report.id!!, replyText)
                    }
                    dialog.dismiss()
                } else {
                    showToast("Empty field")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
