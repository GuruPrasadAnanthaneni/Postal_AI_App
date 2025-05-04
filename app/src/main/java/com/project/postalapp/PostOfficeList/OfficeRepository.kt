package com.project.postalapp.PostOfficeList

import android.content.Context
import com.project.postalapp.room.AppDatabase

class OfficeRepository {

    suspend fun getNearestOffices(
        context: Context
    ): List<Office> {
        val allOffices = AppDatabase.getDatabase(context).reviewDao()
            .getOffices()
        return allOffices.take(15)
    }

    suspend fun searchOffices(
        context: Context,
        officeQuery: String? = null,
        talukaQuery: String? = null,
        districtQuery: String? = null,
        stateQuery: String? = null
    ): List<Office> {
        val baseList = AppDatabase.getDatabase(context).reviewDao().getOffices()

        return baseList
            .filter { office ->
                officeQuery.isNullOrBlank() || office.Office.contains(
                    officeQuery,
                    ignoreCase = true
                )
            }
            .filter { office ->
                talukaQuery.isNullOrBlank() || office.Taluka.contains(
                    talukaQuery,
                    ignoreCase = true
                )
            }
            .filter { office ->
                districtQuery.isNullOrBlank() || office.District.contains(
                    districtQuery,
                    ignoreCase = true
                )
            }
            .filter { office ->
                stateQuery.isNullOrBlank() || office.State.contains(
                    stateQuery,
                    ignoreCase = true
                )
            }
            .let { filteredList ->

                filteredList.sortedBy { it.State }

            }
    }
}