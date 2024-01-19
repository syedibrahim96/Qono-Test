package com.example.explorer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocation(locationEntity: LocationEntity)

    @Query("SELECT * FROM location_table")
    suspend fun getAllLocations(): List<LocationEntity>
}