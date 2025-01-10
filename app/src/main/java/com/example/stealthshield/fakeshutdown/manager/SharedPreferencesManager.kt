package com.example.stealthshield.fakeshutdown.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.stealthshield.fakeshutdown.utility.Constants.APP_PREFS
import com.example.stealthshield.viewmodel.ContactViewModel
import com.example.stealthshield.viewmodel.LocationServiceViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    private var sharedPrefs: SharedPreferences = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)

    fun save(key: String, value: String?) {
        sharedPrefs.edit().putString(key, value).apply()
    }

    fun get(key: String): String? {
        return sharedPrefs.getString(key, null)
    }


////    Tricky way to start the service for location
//    val viewModel= LocationServiceViewModel(context as android.app.Application)
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun startLocationTrackingPhoneStollen(){
//        viewModel.startLocationTrackingPhoneStollen()
//
//    }



    //SMS module
//    var lastKnownLocation = "Chembur"
//    var dateTime = "12:12"
//    val messageBody = "StealthShield Alert:\n\nHello,\nThis is an urgent message from StealthShield Mobile Theft App. My device has been reported stolen.\n\nLast Known Location: $lastKnownLocation \nDate & Time of Incident: $dateTime \n\nPlease help me in locating my device and notify the authorities if you see it.\nThank you for your assistance!"

//    fun sendAlert(){
//        val contactViewModel = ContactViewModel()
//        Log.d("AlertForStollen", "contactViewModel intensiated....")
//        contactViewModel.sendSmsToAllContacts(messageBody)
//        Log.d("AlertForStollen", "sendSmsToAllContacts Completed....")
//
//    }






}