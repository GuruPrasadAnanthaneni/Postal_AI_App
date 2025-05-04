package com.project.postalapp.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.postalapp.PostOfficeList.Office
import com.project.postalapp.model.UserReport

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(office: List<Office>)

    @Query("SELECT * FROM office")
    suspend fun getOffices(): List<Office>

    @Query("SELECT * FROM office")
    suspend fun getOfficesbyQuery(): List<Office>


    @Query(
        """
        SELECT * FROM office 
        WHERE (6371 * ACOS(
            COS(RADIANS(:userLat)) * COS(RADIANS(latitude)) * 
            COS(RADIANS(longitude) - RADIANS(:userLng)) + 
            SIN(RADIANS(:userLat)) * SIN(RADIANS(latitude))
        )) <= :radius
        ORDER BY (6371 * ACOS(
            COS(RADIANS(:userLat)) * COS(RADIANS(latitude)) * 
            COS(RADIANS(longitude) - RADIANS(:userLng)) + 
            SIN(RADIANS(:userLat)) * SIN(RADIANS(latitude))
        ))
    """
    )
    suspend fun getNearbyOffices(
        userLat: Double,
        userLng: Double,
        radius: Double,
    ): List<Office>

    // Search query
    @Query(
        """
        SELECT * FROM office 
        WHERE (:office IS NULL OR Office LIKE '%' || :office || '%')
        AND (:taluka IS NULL OR Taluka LIKE '%' || :taluka || '%')
        AND (:district IS NULL OR District LIKE '%' || :district || '%')
        ORDER BY District, Taluka, Office
    """
    )
    suspend fun searchOffices(
        office: String?,
        taluka: String?,
        district: String?,
    ): List<Office>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReport(report: UserReport): Long

    @Query("SELECT * FROM reviews")
    fun getReviewByUserId(): LiveData<List<Review>>

    @Query("SELECT * FROM report ORDER BY id")
    fun getReports(): LiveData<List<UserReport>>

    @Query("Update report set reply=:reply where id=:id")
    fun updateReport(id: Int, reply: String)
}