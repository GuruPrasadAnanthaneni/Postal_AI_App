package com.project.postalapp.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PincodeViewModel : ViewModel() {
    private val apiService = PincodeApiService.create()

    private val _pincodeData = MutableLiveData<List<PincodeResponse>>()
    val pincodeData: LiveData<List<PincodeResponse>> = _pincodeData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading


    fun fetchPincodeData(pincode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true) // Use postValue for background thread
                val response = apiService.getPostOfficesByName(pincode)
                if (response.isNotEmpty() && response[0].Status == "Success") {
                    _pincodeData.postValue(response) // Use postValue instead of setValue
                } else {
                    _error.postValue(response[0].Message ?: "No data found")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "An error occurred")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
