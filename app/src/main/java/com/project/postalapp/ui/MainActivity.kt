package com.project.postalapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.postalapp.R
import com.project.postalapp.model.RailLocator
import com.project.postalapp.response.RetrofitInstance
import com.project.postalapp.response.RetrofitInstance.TYPE
import com.project.postalapp.utils.SessionManager
import com.project.postalapp.utils.showToast
import com.project.postalapp.response.LoginResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val shared by lazy { SessionManager(applicationContext) }
    private var lat = 0.0
    private var lng = 0.0
    private var userLocation = LatLng(0.0, 0.0)
    private var role: String = ""

    private val serviceStations = mutableListOf<RailLocator>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            initializeLocation()
        } else {
            showToast("Location permission is required to use this feature.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        role = shared.getUserRole() ?: ""

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                initializeLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app requires location access to display service stations near you.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    .setCancelable(false)
                    .show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        loadLocations()
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocation() {
        fused.lastLocation.addOnSuccessListener { location ->
            location?.let {
                lat = it.latitude
                lng = it.longitude
            }
            userLocation = LatLng(lat, lng)
            Log.d("UserLocation", "onCreate: $userLocation")

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        }
    }

    private fun loadLocations() {
        RetrofitInstance.instance.getLocatorStops()
            .enqueue(object : Callback<LoginResponse?> {
                override fun onResponse(
                    call: Call<LoginResponse?>,
                    response: Response<LoginResponse?>,
                ) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        val data = responseBody.data2
                        val locations = when (role) {
                            "Post" -> data.filter { it.role == "Post" }
                            else -> data.filter { it.type == TYPE }
                        }

                        serviceStations.clear()
                        serviceStations.addAll(locations)

                        if (::mMap.isInitialized) {
                            mMap.clear()
                            addUserMarker(userLocation)
                            addServiceStations()
                        }
                    } else {
                        showToast(responseBody?.message ?: "Unknown error")
                    }
                }

                override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                    showToast(t.message ?: "Network Error")
                }
            })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        addUserMarker(userLocation)
        addServiceStations()

        mMap.setOnMarkerClickListener { marker ->
            when (marker.tag) {
                "USER" -> true
                is RailLocator -> {
                    val station = marker.tag as RailLocator
                    showEstimationBottomSheet(station)
                    true
                }

                else -> {
                    showToast("Unknown Marker")
                    true
                }
            }
        }
    }

    private fun addUserMarker(location: LatLng) {
        val userIcon = BitmapDescriptorFactory.fromResource(R.drawable.location)
        val userMarker = mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Your Location")
                .icon(userIcon)
        )
        userMarker?.tag = "USER"
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
    }

    private fun addServiceStations() {
        serviceStations.forEach { station ->
            val iconRes = when (station.role) {
                "Post" -> R.drawable.mail_box
                else -> null
            }
            val stationIcon = iconRes?.let { BitmapDescriptorFactory.fromResource(it) }
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(parseLatLng(station.location))
                    .title(station.stationName)
                    .icon(stationIcon)
            )
            marker?.tag = station
        }
    }

    private fun parseLatLng(location: String): LatLng {
        val parts = location.split(",")
        val lat = parts[0].trim().toDouble()
        val lng = parts[1].trim().toDouble()
        return LatLng(lat, lng)
    }

    private fun showEstimationBottomSheet(station: RailLocator) {
        val stationLatLng = parseLatLng(station.location)
        val distanceKm = haversine(
            userLocation.latitude,
            userLocation.longitude,
            stationLatLng.latitude,
            stationLatLng.longitude
        )
        val formattedDistanceKm = String.format("%.1f km", distanceKm)

        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            val stationAddress = try {
                val addresses = geocoder.getFromLocation(
                    stationLatLng.latitude,
                    stationLatLng.longitude,
                    1
                )
                if (addresses != null && addresses.isNotEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Unknown Location"
                } else {
                    "Unknown Location"
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error getting station name", e)
                "Unknown Location"
            }

            withContext(Dispatchers.Main) {
                ViewPostData(
                    stationName = station.stationName,
                    stationMobile = station.stationMobile,
                    location = stationAddress,
                    chargingPoints = station.chargingpoints ?: "",
                    adapterTypes = station.adapterTypes ?: emptyList(),
                    distance = formattedDistanceKm,
                    role = station.role,
                    station.location
                ).show(
                    supportFragmentManager,
                    "EstimationBottomSheet"
                )
            }
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}