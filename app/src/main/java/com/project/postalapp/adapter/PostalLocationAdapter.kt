package com.project.postalapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.project.postalapp.databinding.ViewstationBinding
import com.project.postalapp.model.RailLocator
import com.project.postalapp.utils.LocationUtils
import com.project.postalapp.utils.spanned

class PostalLocationAdapter(var list: List<RailLocator>) :
    RecyclerView.Adapter<PostalLocationAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ListViewHolder(
        ViewstationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) =
        holder.bind(list[position])

    class ListViewHolder(val binding: ViewstationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RailLocator) {

            val latLng = item.location.split(",").map { it.trim().toDouble() }

            val address =
                LocationUtils.getAddressFromLatLng(binding.root.context, latLng[0], latLng[1])

            if (item.role == "Fuel Station") {
                binding.stationEmail.isVisible = true
            } else {
                binding.stationEmail.isVisible = false
            }


            binding.stationName.text =
                spanned("<b><big>Office Name</big></b>: ${item.stationName}")
            binding.stationMobile.text =
                spanned("<b><big>Office Mobile</big></b>: ${item.stationMobile}")

            binding.stationLocation.text = address
        }

    }

    fun newList(list: List<RailLocator>) {
        this.list = list
        notifyDataSetChanged()

    }


}