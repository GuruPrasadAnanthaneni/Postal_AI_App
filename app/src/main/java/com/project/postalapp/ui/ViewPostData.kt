package com.project.postalapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.postalapp.R
import com.project.postalapp.utils.spanned

class ViewPostData(
    private val stationName: String,
    private val stationMobile: String,
    private val location: String,
    private val chargingPoints: String,
    private val adapterTypes: List<String>,
    private val distance: String,
    private val role: String,
    private val station: String,
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_estimation_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvStationName = view.findViewById<TextView>(R.id.tvStationName)
        val tvStationMobile = view.findViewById<TextView>(R.id.tvStationMobile)
        val tvStationLocation = view.findViewById<TextView>(R.id.tvStationLocation)
        val tvChargingPoints = view.findViewById<TextView>(R.id.tvChargingPoints)
        val tvAdapterTypes = view.findViewById<TextView>(R.id.tvAdapterTypes)
        val tvDistance = view.findViewById<TextView>(R.id.tvDistance)
        val btnCallStation = view.findViewById<Button>(R.id.btnCallStation)
        val btnClose = view.findViewById<Button>(R.id.btnClose)


        val numberedAdapterTypes = adapterTypes.mapIndexed { index, type ->
            "${index + 1}. $type"
        }.joinToString("<br>")


        tvStationName.text = spanned("<b><big>Station Name</big></b>: $stationName")
        tvStationMobile.text = spanned("<b><big>Mobile</big></b>: $stationMobile")
        tvStationLocation.text = spanned("<b><big>Location</big></b>: $location")
        tvChargingPoints.text = spanned("<b><big>Fuel Price</big></b>: $chargingPoints")
        tvDistance.text = spanned("<b><big>Distance</big></b>: $distance")
        tvAdapterTypes.text =
            spanned("<b><big>Available Amenities</big></b>:<br>$numberedAdapterTypes")

        btnCallStation.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$stationMobile")
            }
            startActivity(intent)
        }

        val parts = station.split(",")
        val lat = parts[0].trim().toDouble()
        val lng = parts[1].trim().toDouble()


        tvStationLocation.setOnClickListener {
            val gmmIntentUri =
                Uri.parse("google.navigation:q=${lat},${lng}&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }



        btnClose.setOnClickListener {
            dismiss()
        }
    }


}
