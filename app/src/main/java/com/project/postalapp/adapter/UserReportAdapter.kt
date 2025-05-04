package com.project.postalapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.project.postalapp.databinding.ReportLayoutBinding
import com.project.postalapp.model.UserReport
import com.project.postalapp.utils.spanned
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserReportAdapter(
    private var users: List<UserReport>,
    var onclicked: (UserReport) -> Unit,
    var role: String,
) : RecyclerView.Adapter<UserReportAdapter.UserViewHolder>() {

    private val simple = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    class UserViewHolder(val binding: ReportLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ReportLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        // Handle reply visibility safely
        holder.binding.reply.isVisible = !user.reply.isNullOrEmpty()

        // Fix role case sensitivity issue
        holder.binding.replybutton.isVisible = role.equals("admin", ignoreCase = true)

        // Populate data safely
        holder.binding.usernam.text = spanned("<b>UserName</b>: ${user.userName}")
        holder.binding.usrmobile.text = spanned("<b>UserNumber</b>: ${user.userMobile}")
        holder.binding.report.text = spanned("Report: ${user.issue}")

        // Ensure `postedDate` is a Long
        val dateLong = user.postedDate.toLongOrNull() ?: System.currentTimeMillis()
        holder.binding.posted.text = spanned("Posted: ${simple.format(Date(dateLong))}")

        holder.binding.reply.text = spanned("Reply: ${user.reply ?: "No reply yet"}")

        // Set click listener
        holder.binding.replybutton.setOnClickListener {
            onclicked(user)
        }
    }

    fun updateData(newList: List<UserReport>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = users.size
}
