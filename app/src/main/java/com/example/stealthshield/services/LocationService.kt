package com.example.stealthshield.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import com.example.stealthshield.MainActivity
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.example.stealthshield.R
import com.example.stealthshield.fakeshutdown.utility.Constants.APP_PREFS
import com.example.stealthshield.fakeshutdown.utility.Constants.F_POWER_OFF_ENABLED_KEY
import com.example.stealthshield.viewmodel.SafeZone
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class LocationService : Service() {

    companion object {
        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID = 12345
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var notificationManager: NotificationManager
    private var location: Location? = null
    private val db = FirebaseFirestore.getInstance()
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("HardwareIds")
    private lateinit var deviceId : String
    var deviceName = Build.MODEL
    val deviceBrand = Build.BRAND
    val deviceManufacturer = Build.MANUFACTURER
    var inSafeZone = false


    override fun onCreate() {
        super.onCreate()
        try {
            deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
                ?: UUID.randomUUID().toString()
            Log.d("LocationService", "Device ID: $deviceId")

        } catch (e: Exception) {
            Log.e("LocationService", "Error retrieving device ID", e)
            deviceId = UUID.randomUUID().toString()
        }

        Log.d("LocationService", "Location service created")
        sharedPreferences = getSharedPreferences(APP_PREFS, MODE_PRIVATE)
        initializeLocationService()
        createNotificationChannel()
        setLocationTrackingStatus(true)
    }

    private fun initializeLocationService() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 7000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                super.onLocationResult(locationResult)
                onNewLocation(locationResult)


            }
        }
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to request location updates", e)
        }
    }

    private fun removeLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to remove location updates", e)
        }
        stopForeground(true)
        stopSelf()
    }

    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation
        Log.d("LocationService", "New location received: ${location?.latitude}, ${location?.longitude}")
        CoroutineScope(Dispatchers.IO).launch {
            updateInSafeZone()
        }
        updateLocationInFirestore(location?.latitude, location?.longitude)
    }

    fun calculateDistance(geoPoint1: GeoPoint, geoPoint2: GeoPoint): Double {
        val earthRadius = 6371.0 // Radius of the Earth in kilometers

        val lat1 = geoPoint1.latitude
        val lon1 = geoPoint1.longitude
        val lat2 = geoPoint2.latitude
        val lon2 = geoPoint2.longitude

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c * 1000 // Distance in meters
    }



    suspend fun updateInSafeZone() {
        try {
            val safeZonesQuery = userId?.let {
                db.collection("users").document(it).collection("safeZones").get()
            }
            var isInAnySafeZone = false
            var fenceName = ""
            safeZonesQuery?.await()?.forEach { document ->
                val name = document.getString("fenceName") ?: "Untitled"
                val center = document.getGeoPoint("center") ?: GeoPoint(0.0, 0.0)
                val radius = document.getDouble("radius") ?: 100.0
                val distance = calculateDistance(GeoPoint(location!!.latitude, location!!.longitude), center)

                if ((distance - radius) <= 0.0) {
                    isInAnySafeZone = true
                    fenceName=name
                }
            }

            inSafeZone = isInAnySafeZone
            sharedPreferences.edit().putString("Insafezone", inSafeZone.toString()).apply()
            sharedPreferences.edit().putString("InFence", fenceName).apply()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    private fun updateLocationInFirestore(latitude: Double?, longitude: Double?) {
        if (latitude != null && longitude != null && userId != null) {
            val geoPoint = GeoPoint(latitude, longitude)
            Log.d("LocationService", "Updating location in Firestore for device: $deviceId")
            db.collection("users")
                .document(userId)
                .collection("devices")
                .document(deviceId)
                .set(mapOf("device_location" to geoPoint,
                    "device_name" to deviceName,
                    "device_brand" to deviceBrand,
                    "device_manufacturer" to deviceManufacturer,
                    "device_id" to deviceId,
                    "device_type" to "android",
                    "device_inSafeZone" to inSafeZone,
                ), SetOptions.merge()
                )
                .addOnSuccessListener {
                    Log.d("LocationService", "Location updated in Firestore for device: $deviceId")
                }
                .addOnFailureListener { e ->
                    Log.e("LocationService", "Error updating location in Firestore for device: $deviceId", e)
                }
            //SafeZone Update
        } else {
            Log.e("LocationService", "Invalid location or user ID")
        }
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }


    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service Running")
            .setContentText("Tracking location: Latitude: ${location?.latitude}, Longitude: ${location?.longitude}")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set priority to high to ensure visibility
            .setOngoing(true) // Makes the notification persistent and not dismissible
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, "Location Updates", NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "Location service started")
        createLocationRequest()
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeLocationUpdates()
        setLocationTrackingStatus(false)
        Log.d("LocationService", "Location service destroyed")
        super.onDestroy()
    }


    private fun setLocationTrackingStatus(isTracking: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("LocationTracking", isTracking)
            apply()
        }
    }
}
