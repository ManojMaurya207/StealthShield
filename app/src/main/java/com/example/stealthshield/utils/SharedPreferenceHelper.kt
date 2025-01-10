package com.example.stealthshield.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class SharedPreferenceHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("StealthShieldPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save marker list (LatLng) in shared preferences
    fun saveMarkers(markers: List<LatLng>) {
        val json = gson.toJson(markers)
        sharedPreferences.edit().putString("markers", json).apply()
    }

    // Retrieve marker list (LatLng) from shared preferences
    fun getMarkers(): List<LatLng> {
        val json = sharedPreferences.getString("markers", null) ?: return emptyList()
        val type = object : TypeToken<List<LatLng>>() {}.type
        return gson.fromJson(json, type)
    }

    // Save the radius value in shared preferences
    fun saveRadius(radius: Double) {
        sharedPreferences.edit().putFloat("radius", radius.toFloat()).apply()
    }

    fun getRadius(): Double {
        return sharedPreferences.getFloat("radius", 100f).toDouble() // Default radius is 100 meters
    }

    fun clearData() {
        sharedPreferences.edit().clear().apply()
    }
}
