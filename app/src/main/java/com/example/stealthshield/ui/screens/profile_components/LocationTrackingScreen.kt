package com.example.stealthshield.ui.screens.profile_components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stealthshield.R
import com.example.stealthshield.ui.components.AnimatedImageView
import com.example.stealthshield.viewmodel.LocationServiceViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LocationTrackingScreen() {
    val viewModel: LocationServiceViewModel = viewModel()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    var isInternetAvailable by remember { mutableStateOf(true) }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                hasNotificationPermission = true
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            hasNotificationPermission = true // No need for permission before Android 13
        }

        isInternetAvailable = checkInternetConnectivity(context)
        if (!isInternetAvailable) {
            snackbarHostState.showSnackbar("No internet connection.")
        }
    }

    LaunchedEffect(hasLocationPermission, hasNotificationPermission) {
        if (!hasLocationPermission) {
            snackbarHostState.showSnackbar("Cannot start location tracking: Location permission is denied.")
        }

        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            snackbarHostState.showSnackbar("Notification permission is required to show notifications.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {

        LocationServiceControl(
            isServiceRunning = isServiceRunning,
            onServiceToggle = { shouldStart ->
                viewModel.toggleService(shouldStart)
            },
            hasLocationPermission = hasLocationPermission,
            isInternetAvailable = isInternetAvailable,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
fun LocationServiceControl(
    isServiceRunning: Boolean,
    onServiceToggle: (Boolean) -> Unit,
    hasLocationPermission: Boolean,
    isInternetAvailable: Boolean,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Location Tracking",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Crossfade(
            targetState = isServiceRunning,
            animationSpec = tween(durationMillis = 500) // Adjust the duration as needed
        ) { tracking ->
            AnimatedImageView(
                rawResId = if (tracking) R.raw.location_tracking else R.raw.location_not_tracking,
                modifier = Modifier
                    .size(300.dp)
                    .padding(vertical = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tracking Status:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(2f)
            )
            Switch(
                checked = isServiceRunning,
                onCheckedChange = { isChecked ->
                    onServiceToggle(isChecked)
                    coroutineScope.launch {
                        if (isChecked) {
                            snackbarHostState.showSnackbar("Location is being tracked.")
                        } else {
                            snackbarHostState.showSnackbar("Location is not being tracked.")
                        }
                    }
                },
                enabled = hasLocationPermission
            )
        }

        if (!hasLocationPermission) {
            Text(
                text = "Please enable location permissions in settings to track location.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (!isInternetAvailable) {
            Text(
                text = "Please check your internet connection to track location.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))
        // Instructive Text
        Text(
            text = "Enable location tracking to allow the app to monitor your location and provide relevant services.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = "Switch on the tracking status to start receiving real-time location updates.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}


fun checkInternetConnectivity(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
