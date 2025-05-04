package com.project.postalapp.PostOfficeList

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OfficeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfficeRepository()

    val nearestOffices = MutableLiveData<List<Office>>()
    val searchResults = MutableLiveData<List<Office>>()
    val isLoading = MutableLiveData<Boolean>()

    fun loadNearestOffices(context: Context) {

        viewModelScope.launch {
            isLoading.value = true
            nearestOffices.value = repository.getNearestOffices(context)
            isLoading.value = false
        }
    }

    fun search(
        context: Context,
        officeQuery: String? = null,
        talukaQuery: String? = null,
        districtQuery: String? = null,
        stateQuery: String? = null,
    ) {
        viewModelScope.launch {
            isLoading.value = true
            searchResults.value = repository.searchOffices(
                context,
                officeQuery = officeQuery,
                talukaQuery = talukaQuery,
                districtQuery = districtQuery,
                stateQuery
            )
            isLoading.value = false
        }
    }
}