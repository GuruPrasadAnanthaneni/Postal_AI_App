package com.project.postalapp.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.postalapp.databinding.ViewAnnoucementsBinding
import com.project.postalapp.model.Announcements


class SchemesAdapter(var list: List<Announcements>, var onClickWeb: (Announcements) -> Unit) :
    RecyclerView.Adapter<SchemesAdapter.ViewSchemes>() {
    class ViewSchemes(val binding: ViewAnnoucementsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(scheme: Announcements) {
            binding.header.text = scheme.aName
            binding.posteddate.text = scheme.aDate
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewSchemes {
        return ViewSchemes(ViewAnnoucementsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewSchemes, position: Int) {
        holder.bind(list[position])
        holder.binding.root.setOnClickListener {
            onClickWeb(list[position])
        }
    }
}