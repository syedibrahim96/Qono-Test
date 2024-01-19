package com.example.explorer

import android.Manifest
import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class LocationService: Service() {
    private lateinit var locationHandler: Handler
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationDao: LocationDao
    private lateinit var locationDatabase: LocationDatabase

    override fun onCreate() {
        super.onCreate()
        locationDatabase = Room.databaseBuilder(
            applicationContext,
            LocationDatabase::class.java, "location_database"
        ).build()

        locationDao = locationDatabase.locationDao()

        locationHandler = Handler(Looper.getMainLooper())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        locationHandler.postDelayed(object : Runnable {
            override fun run() {
                requestLocation()
                locationHandler.postDelayed(this, 5000) // Fetch location every 5 seconds
            }
        }, 0) // Initial delay before the first fetch
    }


    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
//                    println("Latitude: $latitude, Longitude: $longitude")

                    // Get the current date and time
                    val currentTime = Calendar.getInstance().time

                    // Format the date and time using SimpleDateFormat
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val formattedDateTime = sdf.format(currentTime)

                    val locationvalue = LocationEntity(latitude = latitude, longitude = longitude, timestamp = formattedDateTime)
                    saveLocationToDatabase(locationvalue)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting location: $exception")
            }
    }

    private fun saveLocationToDatabase(locationEntity: LocationEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            locationDao.insertLocation(locationEntity)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop any ongoing tasks or cleanup if needed
    }
}