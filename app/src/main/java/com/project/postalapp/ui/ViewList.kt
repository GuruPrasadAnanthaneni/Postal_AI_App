package com.project.postalapp.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.postalapp.adapter.PostalLocationAdapter
import com.project.postalapp.databinding.ActivityViewListBinding
import com.project.postalapp.response.RetrofitInstance
import com.project.postalapp.response.RetrofitInstance.TYPE
import com.project.postalapp.utils.SessionManager
import com.project.postalapp.utils.showToast
import com.project.postalapp.response.LoginResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewList : AppCompatActivity() {
    private val bind by lazy { ActivityViewListBinding.inflate(layoutInflater) }
    private val shared by lazy { SessionManager(applicationContext) }
    private lateinit var stationAdapter: PostalLocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)


        loadStations()
        stationAdapter = PostalLocationAdapter(emptyList())
        bind.recycleList.adapter = stationAdapter
        bind.recycleList.setHasFixedSize(true)
        bind.recycleList.layoutManager = LinearLayoutManager(applicationContext)

    }

    private fun loadStations() {
        CoroutineScope(IO).launch {
            RetrofitInstance.instance.getLocatorStops()
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        p0: Call<LoginResponse>,
                        p1: Response<LoginResponse>,
                    ) {
                        val response = p1.body()!!


                        Log.d("fkdkfhkdshfk", "Response body: ${response.data2}")

                        if (!response.error) {
                            val station =
                                response.data2.filter { it.role == "Post" && it.type == TYPE }
                            val station2 = station.sortedByDescending { it.id }

                            if (station2.isNotEmpty()) {
                                showToast("Successful")
                                stationAdapter.newList(station2)
                            } else {
                                showToast("No stations available")
                            }
                        } else {
                            // Handle case where response is null or error flag is true
                            showToast("Failed to load the list")
                        }
                    }

                    override fun onFailure(p0: Call<LoginResponse?>, p1: Throwable) {
                        showToast(p1.message ?: "Unknown error occurred")
                    }
                })
        }
    }
}