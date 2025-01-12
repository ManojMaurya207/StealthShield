package com.example.stealthshield

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stealthshield.navigation.StealthNavigation
import com.example.stealthshield.ui.theme.StealthShieldTheme
import com.example.stealthshield.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel = AuthViewModel(this, Identity.getSignInClient(this))
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "<Google-api-key>")  // Replace with your API key
        }


        setContent {
            StealthShieldTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var isInternetAvailable = checkInternetConnectivity(this)

                LaunchedEffect(isInternetAvailable) {
                    if (!isInternetAvailable) {
                        snackbarHostState.showSnackbar("No internet connection.", duration = SnackbarDuration.Long)
                    }
                }

                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 85.dp)) },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    StealthNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
    }

    fun checkInternetConnectivity(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}
