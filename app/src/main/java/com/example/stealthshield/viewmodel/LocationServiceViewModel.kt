package com.example.stealthshield.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stealthshield.fakeshutdown.utility.Constants.APP_PREFS
import com.example.stealthshield.services.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class LocationServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(APP_PREFS, Application.MODE_PRIVATE)
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> get() = _isServiceRunning



    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleService(shouldStart: Boolean) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val intent = Intent(context, LocationService::class.java)

            if (shouldStart && !isServiceRunning.value) {
                context.startForegroundService(intent) // Use startForegroundService instead of startService
                _isServiceRunning.value = true
                saveServiceStatus(true)
            } else if (!shouldStart && isServiceRunning.value) {
                context.stopService(intent)
                _isServiceRunning.value = false
                saveServiceStatus(false)
            }
        }
    }

    init {
        _isServiceRunning.value = getServiceStatus()
    }

    private fun getServiceStatus(): Boolean {
        return sharedPreferences.getBoolean("LocationTracking", false)
    }

    private fun saveServiceStatus(isRunning: Boolean) {
        sharedPreferences.edit().putBoolean("LocationTracking", isRunning).apply()
    }





    //For the tricky method of the phone stolen tracking service
    @RequiresApi(Build.VERSION_CODES.O)
    fun startLocationTrackingPhoneStollen(){
        val context = getApplication<Application>()
        val intent = Intent(context, LocationService::class.java)
        context.startForegroundService(intent)
        _isServiceRunning.value = true
        saveServiceStatus(true)
    }

}
