package com.project.postalapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.project.postalapp.databinding.ActivityAddstationBinding
import com.project.postalapp.model.PostOfficeAmenity
import com.project.postalapp.response.CommonResponse
import com.project.postalapp.response.RetrofitInstance
import com.project.postalapp.utils.LocationUtils
import com.project.postalapp.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPostOffice : AppCompatActivity() {

    private val binding by lazy { ActivityAddstationBinding.inflate(layoutInflater) }
    private lateinit var gestureDetector: GestureDetector
    private lateinit var getRequestLauncher: ActivityResultLauncher<Intent>
    private var lat: Double? = null
    private var lng: Double? = null


    @SuppressLint("ClickableViewAccessibility", "ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val intent = Intent(this@AddPostOffice, MapsActivity::class.java)
                getRequestLauncher.launch(intent)
                return true
            }
        })

        getRequestLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = it.data
                    lat = data?.getDoubleExtra("latitude", 0.0)
                    lng = data?.getDoubleExtra("longitude", 0.0)

                    if (lat != null && lng != null) {
                        val address = LocationUtils.getAddressFromLatLng(
                            applicationContext,
                            lat!!,
                            lng!!
                        )
                        showToast("$lat,$lng")

                        binding.etLocation.setText(address)
                    }
                }
            }

        binding.etLocation.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        binding.buttonConfirm.setOnClickListener {

            val stationName = binding.etStationName.text.toString().trim()
            val stationMobile = binding.etStationMobile.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val selectedAmenity = getSelectedAmenities()


            when {
                stationName.isEmpty() -> showToast("Please enter the station name")
                stationMobile.isEmpty() -> showToast("Please enter the station mobile number")
                location.isEmpty() -> showToast("Please enter the location")
                selectedAmenity.isEmpty() -> showToast("Please select at least one adapter type")

                else -> {
                    binding.loadingIndicator.visibility = View.VISIBLE
                    binding.buttonConfirm.isEnabled = false

                    CoroutineScope(Dispatchers.IO).launch {

                        try {
                            RetrofitInstance.instance.addStation(
                                officeName = stationName,
                                officeMobile = stationMobile,
                                location = "$lat,$lng",
                                MailBox = "fuelPrice",
                                type = RetrofitInstance.TYPE,
                                role = "Post",
                                amenities = selectedAmenity
                            ).enqueue(object : Callback<CommonResponse?> {
                                override fun onResponse(
                                    call: Call<CommonResponse?>,
                                    response: Response<CommonResponse?>,
                                ) {
                                    runOnUiThread {
                                        binding.loadingIndicator.visibility = View.GONE
                                        binding.buttonConfirm.isEnabled = true
                                        if (response.isSuccessful) {
                                            val result = response.body()
                                            if (result != null && !result.error) {
                                                showToast(result.message)
                                                finish()
                                            } else {
                                                showToast("Failed to add the Station")
                                            }
                                        } else {
                                            showToast("Error: ${response.message()}")
                                        }
                                    }
                                }

                                override fun onFailure(
                                    call: Call<CommonResponse?>,
                                    t: Throwable,
                                ) {
                                    runOnUiThread {
                                        binding.loadingIndicator.visibility = View.GONE
                                        binding.buttonConfirm.isEnabled = true
                                        showToast("Network error: ${t.message}")
                                    }
                                }
                            })

                        } catch (e: Exception) {
                            runOnUiThread {
                                binding.loadingIndicator.visibility = View.GONE
                                binding.buttonConfirm.isEnabled = true
                                showToast("Network error: ${e.message}")
                            }
                        }
                    }

                }
            }


        }


    }


    private fun getSelectedAmenities(): List<String> {

        val selectedAdapters = mutableListOf<String>()
        if (binding.checkboxType1.isChecked) selectedAdapters.add(PostOfficeAmenity.PARKING.displayName)
        if (binding.checkboxType2.isChecked) selectedAdapters.add(PostOfficeAmenity.MAILBOX.displayName)
        if (binding.checkboxType3.isChecked) selectedAdapters.add(PostOfficeAmenity.WAITING_AREA.displayName)
        if (binding.checkboxType8.isChecked) selectedAdapters.add(PostOfficeAmenity.ATM.displayName)
        if (binding.checkboxType9.isChecked) selectedAdapters.add(PostOfficeAmenity.SERVICE_COUNTER.displayName)
        return selectedAdapters


    }
}

