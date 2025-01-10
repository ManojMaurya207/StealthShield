package com.example.stealthshield.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore

    private val _loggedInDevices = MutableLiveData<Int>()
    val loggedInDevices: LiveData<Int> get() = _loggedInDevices

    private val _totalSafeZones = MutableLiveData<Int>()
    val totalSafeZones: LiveData<Int> get() = _totalSafeZones

    private val _activeSOSContacts = MutableLiveData<Int>()
    val activeSOSContacts: LiveData<Int> get() = _activeSOSContacts


    fun fetchData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        fetchLoggedInDevices(userId)

        fetchSafeZoneCount(userId)

        fetchActiveSOSContacts(userId)

    }

    private fun fetchLoggedInDevices(userId: String) {
        var deviceNum = 0 // Use 'var' to make this variable mutable
        db.collection("users").document(userId).collection("devices")
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { device ->
                    val isLoggedIn = device.getBoolean("device_login_status") ?: false
                    if (isLoggedIn) deviceNum += 1
                }
                _loggedInDevices.value = deviceNum // Set the value to the count of logged-in devices
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Failed to fetch logged-in devices count: ${exception.message}")
                _loggedInDevices.value = 0 // Handle error case
            }
    }

    private fun fetchSafeZoneCount(userId: String) {
        db.collection("users").document(userId).collection("safeZones")
            .get()
            .addOnSuccessListener { querySnapshot ->
                _totalSafeZones.value = querySnapshot.size()
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Failed to fetch safe zone count: ${exception.message}")
                _totalSafeZones.value = 0 // Handle error case
            }
    }

    private fun fetchActiveSOSContacts(userId: String) {
        db.collection("users").document(userId).collection("emergency_contacts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                _activeSOSContacts.value = querySnapshot.size()
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Failed to fetch active SOS contacts count: ${exception.message}")
                _activeSOSContacts.value = 0 // Handle error case
            }
    }

}
