package com.project.postalapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.postalapp.adapter.ReviewDialog
import com.project.postalapp.databinding.ActivityProfileBinding
import com.project.postalapp.room.AppDatabase
import com.project.postalapp.room.Review
import com.project.postalapp.utils.SessionManager
import com.project.postalapp.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private val binding by lazy { ActivityProfileBinding.inflate(layoutInflater) }
    private val shared by lazy { SessionManager(this) }
    private val db by lazy { AppDatabase.getDatabase(applicationContext).reviewDao() }
    var userId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        userId = "${shared.getUserId()}"

        binding.logoutprofile.setOnClickListener {
            db.getReviewByUserId().observe(this) { review ->
                val list = review.filter { it.userId == userId }
                if (list.isNotEmpty()) {
                    logoutlogic()
                } else {
                    showReviewDialog()
                }
            }

        }



        binding.editTextName.text = "${shared.getUserName()}"
        binding.editTextAdress.text = "${shared.getUserLocation()}"
        binding.editTextEmail.text = "${shared.getUserEmail()}"
        binding.editTextMoblie.text = "${shared.getUserMobile()}"
        binding.editTextPassword.text = "${shared.getUserPassword()}"


    }

    private fun showReviewDialog() {
        val dialog = ReviewDialog(userId) { rating, comment ->
            val review = Review(userId, rating, comment)
            CoroutineScope(Dispatchers.IO).launch {
                val check = db.insertReview(review)
                if (check > 0) {
                    runOnUiThread {
                        logoutlogic()
                        showToast("Review submitted successfully")
                    }

                } else {
                    showToast("Failed to submit review")
                }
            }
        }
        dialog.show(supportFragmentManager, "ReviewDialog")
    }

    private fun logoutlogic() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Confirm") { _, _ ->
                shared.clearLoginState()
                finishAffinity()
                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            }
            .setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}