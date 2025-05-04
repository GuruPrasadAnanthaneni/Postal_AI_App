package com.project.postalapp.PostOfficeList

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.postalapp.response.LoginResponse
import com.project.postalapp.response.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostOfficeViewmodel(application: Application) : AndroidViewModel(application) {

    // Change to the correct type (assuming data4 is a List<Office>)
    private val _list = MutableLiveData<List<Office>>()
    val list: MutableLiveData<List<Office>> = _list

    fun getData() {
        viewModelScope.launch {
            RetrofitInstance.instance.getPostOfficeData().enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>,
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.let {
                            val officeList = it.data4 // Assuming data4 is List<Office>
                            _list.value = officeList
                            Log.d("API_SUCCESS", "Response: ${response.body()}")
                        } ?: run {
                            Log.d("API_ERROR", "Empty response body")
                        }
                    } else {
                        Log.d("API_ERROR", "Unsuccessful response: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable,
                ) {
                    Log.e("API_FAILURE", "Error: ${t.message}", t)
                }
            })
        }
    }
}