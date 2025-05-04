package com.project.postalapp.PostOfficeList

import android.content.Intent
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.project.postalapp.databinding.ItemOfficeBinding
import com.project.postalapp.utils.spanned
import java.util.Locale

class OfficeAdapter(private var officeList: List<Office>) :
    RecyclerView.Adapter<OfficeAdapter.ListviewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListviewHolder {
        return ListviewHolder(
            ItemOfficeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ListviewHolder, position: Int) {
        val list = officeList[position]
        Log.d("OfficeAdapter", "Office: ${list.Office}, Latitude: ${list.Latitude}")
        val geocoder = Geocoder(holder.itemView.context, Locale.getDefault())
        holder.binding.apply {
            tvOffice.text = spanned("<b>Post Office</b>: ${list.Office}")
            tvTaluka.text = spanned("<b>Taluka</b>: ${list.Taluka}")
            tvDistrict.text = spanned("<b>District</b>: ${list.District}")
            try {
                val latitudeof = list.Latitude.toDouble()
                val longitudeof = list.Longitude.toDouble()
                val address = try {
                    geocoder.getFromLocation(latitudeof, longitudeof, 1)
                } catch (e: Exception) {
                    Log.e("OfficeAdapter", "Geocoder error", e)
                    null
                }
                tvAdress.text = address?.firstOrNull()?.getAddressLine(0) ?: "Address not available"
                locationicon.setOnClickListener {
                    val uri = "google.navigation:q=$latitudeof,$longitudeof&mode=m".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    holder.itemView.context.startActivity(intent)
                }
            } catch (e: NumberFormatException) {
                Log.e("OfficeAdapter", "Invalid latitude/longitude: ${list.Latitude}, ${list.Longitude}", e)
                tvAdress.text = "Invalid location data"
            }
        }
    }

    override fun getItemCount(): Int = officeList.size

    class ListviewHolder(val binding: ItemOfficeBinding) : RecyclerView.ViewHolder(binding.root)

    fun updatelist(newList: List<Office>) {
        officeList = newList
        notifyDataSetChanged()
    }
}