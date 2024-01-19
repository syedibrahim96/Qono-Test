package com.example.explorer

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var locationHandler: Handler
    private lateinit var recyclerview_data:RecyclerView
    private lateinit var locationDao: LocationDao
    private lateinit var locationDatabase: LocationDatabase
    lateinit var adapter: CoordinateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerview_data = findViewById(R.id.recyclerview_data)
        recyclerview_data.layoutManager = LinearLayoutManager(this)
        adapter = CoordinateAdapter(mutableListOf())
        // Set the adapter to the RecyclerView
        recyclerview_data.adapter = adapter

        locationDatabase = Room.databaseBuilder(
            applicationContext,
            LocationDatabase::class.java, "location_database"
        ).build()

        locationDao = locationDatabase.locationDao()
        locationHandler = Handler(Looper.getMainLooper())

        if (!foregroundServiceRunning()) {
            requestPermission()
        } else {
            viewLocationDetails()
        }
    }
    fun foregroundServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (LocationService::class.java.getName() == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_CODE) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                PERMISSION_REQUEST_CODE_BG)
            return
        }
        for(i in permissions.indices) {
            if (permissions[i] == android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
                }
        }
    }

    private fun startLocationUpdates() {
        val serviceIntent = Intent(this, LocationService::class.java)
        startService(serviceIntent)
        viewLocationDetails()
    }

    fun viewLocationDetails() {
        locationHandler.postDelayed(object : Runnable {
            override fun run() {
                updateLocalAdapter()
                locationHandler.postDelayed(this, 5000) // Fetch location every 5 seconds
            }
        }, 0)// Initial delay before the first fetch
    }

    private fun updateLocalAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            adapter.updateData(getAllLocationFromDataBase())
        }
    }

    suspend fun getAllLocationFromDataBase(): List<LocationEntity> {
        return locationDao.getAllLocations()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val PERMISSION_REQUEST_CODE_BG = 2
    }
}