package com.example.stealthshield.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stealthshield.fakeshutdown.utility.Constants.APP_PREFS
import com.example.stealthshield.fakeshutdown.utility.Constants.DEVICE_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MapViewModel(context: Context) : ViewModel() {

    private val gson = Gson()
    private val currentLocationKey = "current_location"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var selectedRadius = mutableDoubleStateOf(100.0)
        private set

    var placeTitle = mutableStateOf("Untitled")
        private set

    var circleOptions = mutableStateOf<CircleOptions?>(null)
        private set

    var isSliderDialogOpen = mutableStateOf(false)
        private set

    var isBottomSheetOpen = mutableStateOf(false)
        private set

    var selectedSafeZone = mutableStateOf<SafeZone?>(null)

    var safeZones = mutableStateListOf<SafeZone>()

    var deviceId :String?=""


    //Find My location code
    var Mydevice_inSafeZone :Boolean = false
    val Devices = mutableStateListOf<Devices>()
    var currentLocation = mutableStateOf<LatLng?>(null)




    fun initDataLoad(context: Context) {
        fetchDevicesFromFirestore()
        loadSafeZonesFromFireStore()
        loadCurrentLocation()
    }

    fun updateSafeZoneRadius(safeZone: SafeZone){
        safeZones.remove(selectedSafeZone.value)
        safeZones.add(safeZone)
        updateAllSafeZonesInFirestore()
        val lastSafeZone = safeZones.lastOrNull()
        circleOptions.value = lastSafeZone?.centerLatlng?.let {
            CircleOptions()
                .center(it)
                .radius(safeZone.radius)
        }
    }

    fun setShowBottomSheet(bottomSheetVisible: Boolean) {
        isBottomSheetOpen.value = bottomSheetVisible
    }

    fun addSafeZone(latLng: LatLng) {
        selectedRadius.doubleValue = 100.0
        placeTitle.value = "Untitled"
        circleOptions.value = null
        isSliderDialogOpen.value = true
        val newSafeZone = SafeZone(latLng, selectedRadius.doubleValue, placeTitle.value)
        safeZones.add(newSafeZone)
    }

    fun updatePlaceTitle(newTitle: String) {
        placeTitle.value = newTitle

        val lastSafeZone = safeZones.lastOrNull()
        if (lastSafeZone != null) {
            safeZones[safeZones.lastIndex] = lastSafeZone.copy(name = newTitle)
        }
    }

    fun updateRadius(radius: Double) {
        selectedRadius.value = radius

        val lastSafeZone = safeZones.lastOrNull()
        if (lastSafeZone != null ) {
            safeZones[safeZones.lastIndex] = lastSafeZone.copy(radius = radius)
        }
        circleOptions.value = lastSafeZone?.centerLatlng?.let {
            CircleOptions()
                .center(it)
                .radius(radius)
        }
    }

    fun hideSliderDialog(popOut: Boolean) {
        if (popOut && safeZones.isNotEmpty()) {
            val removedSafeZone = safeZones.removeAt(safeZones.lastIndex)
            removeSafeZoneFromFirestore(removedSafeZone)
        }
        isSliderDialogOpen.value = false
        updateAllSafeZonesInFirestore()
        loadSafeZonesFromFireStore()
    }


    fun loadSafeZonesFromFireStore() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val safeZonesQuery = firestore.collection("users").document(userId).collection("safeZones").get()
                val loadedSafeZones = safeZonesQuery.await().map { document ->
                    val name = document.getString("fenceName") ?: "Untitled"
                    val center = document.getGeoPoint("center") ?: GeoPoint(0.0, 0.0)
                    val radius = document.getDouble("radius") ?: 100.0
                    SafeZone(
                        centerLatlng = LatLng(center.latitude, center.longitude),
                        radius = radius,
                        name = name,
                    )
                }
                withContext(Dispatchers.Main) {
                    safeZones.clear()
                    safeZones.addAll(loadedSafeZones)
                }
                update_insafezone()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




    private fun updateAllSafeZonesInFirestore() {
        val userId = auth.currentUser?.uid ?: return
        sharedPreferences.edit().putString("Insafezone", Mydevice_inSafeZone.toString()).apply()

        viewModelScope.launch(Dispatchers.IO) {
            for (safeZone in safeZones) {
                val safeZoneRef = firestore.collection("users").document(userId).collection("safeZones").document(safeZone.name)
                val safeZoneData = mapOf(
                    "fenceName" to safeZone.name,
                    "center" to GeoPoint(safeZone.centerLatlng.latitude, safeZone.centerLatlng.longitude),
                    "radius" to safeZone.radius,
                    "updated_time" to Date()
                )
                safeZoneRef.set(safeZoneData).addOnSuccessListener {

                    firestore.collection("users")
                        .document(userId)
                        .collection("devices")
                        .document(deviceId!!)
                        .set(mapOf("device_inSafeZone" to Mydevice_inSafeZone), SetOptions.merge())
                        .addOnFailureListener {
                            Log.d("device_inSafeZone", "updateAllSafeZonesInFirestore: ${it.message}")
                        }

                }
            }
        }
    }

    fun removeSafeZoneFromFirestore(safeZone: SafeZone) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val safeZoneRef = firestore.collection("users").document(userId).collection("safeZones").document(safeZone.name)
            safeZoneRef.delete().addOnSuccessListener {
                selectedSafeZone.value=null
                isBottomSheetOpen.value=false
                loadSafeZonesFromFireStore()
            }.addOnFailureListener {
                // Handle failure if needed
            }
        }
    }


    fun update_insafezone(){
        deviceId = sharedPreferences.getString(DEVICE_ID,"")
        Log.d("TAG_FOR_DEVICE", "update_insafezone: $deviceId  list size :${Devices.size}")
        Devices.forEach{
            Log.d("TAG_FOR_DEVICE", "update_insafezone for each : ${it.deviceId}")
            if(it.deviceId == deviceId && it.device_inSafeZone){

                Mydevice_inSafeZone = true
            }else{
                Mydevice_inSafeZone = false
            }
        }
    }


    fun updateCurrentLocation(latLng: LatLng) {
        currentLocation.value = latLng
        viewModelScope.launch(Dispatchers.IO) {
            val curLocJson = gson.toJson(latLng)
            sharedPreferences.edit().putString(currentLocationKey, curLocJson).apply()
        }
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            val curLocJson = sharedPreferences.getString(currentLocationKey, null)
            if (!curLocJson.isNullOrEmpty()) {
                val loadedLocation: LatLng = gson.fromJson(curLocJson, LatLng::class.java)
                currentLocation.value = loadedLocation
            }
        }
    }


    fun fetchDevicesFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        Devices.clear()
        viewModelScope.launch {
            try {
                val result = firestore.collection("users")
                    .document(userId)
                    .collection("devices")
                    .get()
                    .await()
                result.documents.forEach { document ->
                    val location = document.getGeoPoint("device_location")
                    val deviceName = document.getString("device_name") ?: "Unknown Device"
                    val deviceId = document.getString("device_id") ?: "Unknown ID"
                    val deviceBrand = document.getString("device_brand") ?: "Unknown Brand"

                    if (location != null) {
                        Devices.add(
                            Devices(
                                deviceId = deviceId,
                                deviceName = deviceName,
                                device_location = LatLng(location.latitude, location.longitude),
                                device_brand = deviceBrand,
                                device_inSafeZone = document.getBoolean("device_inSafeZone") ?: false
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }





    @SuppressLint("MissingPermission")
    fun initializeLocationAsync(
        fusedLocationClient: FusedLocationProviderClient,
        context: Context,
        onInitialized: () -> Unit
    ) {
        initDataLoad(context)
        CoroutineScope(Dispatchers.IO).launch {
            fetchCurrentLocationAsync(fusedLocationClient)?.let { location ->
                withContext(Dispatchers.Main) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    updateCurrentLocation(latLng)
                    onInitialized()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocationAsync(
        fusedLocationClient: FusedLocationProviderClient
    ): Location? = suspendCoroutine { continuation ->
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                continuation.resume(location)
            } else {
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { newLocation ->
                    continuation.resume(newLocation)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            }
        }.addOnFailureListener {
            continuation.resume(null)
        }
    }

    fun searchPlacesAsync(query: String, placesClient: PlacesClient, onResult: (List<AutocompletePrediction>) -> Unit) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = placesClient.findAutocompletePredictions(request).await()
                withContext(Dispatchers.Main) {
                    onResult(response.autocompletePredictions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun fetchPlaceLatLngAsync(placeId: String, placesClient: PlacesClient, onResult: (LatLng?) -> Unit) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = placesClient.fetchPlace(request).await()
                withContext(Dispatchers.Main) {
                    onResult(response.place.latLng)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }
}







data class SafeZone(
    val centerLatlng: LatLng,
    var radius: Double,
    val name: String,
)

data class Devices(
    val deviceId: String,
    val deviceName: String,
    val device_location: LatLng,
    val device_brand: String,
    val device_inSafeZone : Boolean = false,
)








