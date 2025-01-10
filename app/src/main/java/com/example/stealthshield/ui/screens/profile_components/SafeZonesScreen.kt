package com.example.stealthshield.ui.screens.profile_components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.stealthshield.viewmodel.MapViewModel
import com.example.stealthshield.viewmodel.SafeZone
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SafeZonesScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
//    @SuppressLint("HardwareIds")
//    var deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//        ?: UUID.randomUUID().toString()

    val coroutineScope = rememberCoroutineScope()
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.4223, -122.0848), 12f) // Default location
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            mapViewModel.initializeLocationAsync(fusedLocationClient, context) {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val placesClient: PlacesClient = remember { Places.createClient(context) }
    val predictions = remember { mutableStateListOf<AutocompletePrediction>() }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
            mapViewModel.initializeLocationAsync(fusedLocationClient, context) {
                isLoading = false
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(mapViewModel.currentLocation.value) {
        mapViewModel.currentLocation.value?.let { latLng ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(latLng.latitude,latLng.longitude), 15f)
            isLoading = false
        }
    }

    // UI
    if (isLoading) {
        LoadingScreen(modifier)
    } else {
        MainContent(
            modifier = modifier,
            hasLocationPermission = hasLocationPermission,
            searchQuery = searchQuery,
            onSearchQueryChange = { query ->
                searchQuery = query
                mapViewModel.searchPlacesAsync(query, placesClient) { suggestions ->
                    predictions.clear()
                    predictions.addAll(suggestions)
                    expanded = true
                }
            },
            predictions = predictions,
            onPredictionClick = { prediction ->
                searchQuery = prediction.getFullText(null).toString()
                expanded = false
                mapViewModel.fetchPlaceLatLngAsync(prediction.placeId, placesClient) { latLng ->
                    if (latLng != null) {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder()
                                        .target(latLng)
                                        .zoom(18f)
                                        .tilt(70f)
                                        .build()
                                ),
                                durationMs = 2000
                            )
                        }
                    }
                }
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            cameraPositionState = cameraPositionState,
            mapViewModel = mapViewModel,
            coroutineScope= coroutineScope,
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    modifier: Modifier,
    hasLocationPermission: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    predictions: List<AutocompletePrediction>,
    onPredictionClick: (AutocompletePrediction) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    cameraPositionState: CameraPositionState,
    mapViewModel: MapViewModel,
    coroutineScope: CoroutineScope,
) {
    val bottomSheetState = rememberModalBottomSheetState()

    val isBottomSheetVisible by remember { mapViewModel.isBottomSheetOpen }
    val selectedSafeZone by remember { mapViewModel.selectedSafeZone }

    LaunchedEffect(isBottomSheetVisible) {
        if (isBottomSheetVisible) {
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }
    }




    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Manage Safe Zones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (hasLocationPermission) {
            // Search bar for location queries
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                predictions = predictions,
                onPredictionClick = onPredictionClick,
                expanded = expanded,
                onExpandedChange = onExpandedChange
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box (
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ){
                // Map view displaying current location and safe zones
                MapView(
                    cameraPositionState = cameraPositionState,
                    mapViewModel = mapViewModel,
                    coroutineScope = coroutineScope,
                )
//                ShowSafeZones(mapViewModel)
            }

            // Display safe zone radius slider dialog if requested
            if (mapViewModel.isSliderDialogOpen.value) {
                SafeZoneRadiusDialog(mapViewModel = mapViewModel)
            }
        } else {
            // Show a message and loading indicator while waiting for location permission
            Text(text = "Waiting for location permission...", fontSize = 16.sp)
            CircularProgressIndicator()
        }
        if (mapViewModel.isBottomSheetOpen.value) {
            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = {
                    mapViewModel.setShowBottomSheet(false)
                    mapViewModel.selectedSafeZone.value = null
                },
            ) {
                SafeZoneOptionsBottomSheet(
                    mapViewModel = mapViewModel,
                    safeZone = selectedSafeZone!!
                )
            }

        } else {
            Text("No Safe Zone Selected")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    predictions: List<AutocompletePrediction>,
    onPredictionClick: (AutocompletePrediction) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search for places") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor() // Anchor for the dropdown menu
                .padding(8.dp),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close, // Use the close icon to indicate clearing
                            contentDescription = "Clear text"
                        )
                    }
                }
            }
        )


        ExposedDropdownMenu(
            expanded = expanded && predictions.isNotEmpty(),
            onDismissRequest = { onExpandedChange(false) }
        ) {
            predictions.forEach { prediction ->
                DropdownMenuItem(
                    text = {
                        Text(text = prediction.getFullText(null).toString())
                    },
                    onClick = { onPredictionClick(prediction) },
                    modifier = Modifier.fillMaxWidth() // Ensure dropdown items take full width
                )
            }
        }
    }
}


@Composable
fun MapView(
    cameraPositionState: CameraPositionState,
    mapViewModel: MapViewModel,
    coroutineScope: CoroutineScope
) {

    val context = LocalContext.current
    val currentLocation = mapViewModel.currentLocation.value ?: LatLng(0.0, 0.0)

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp)),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            isBuildingEnabled = true,
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = false,
            tiltGesturesEnabled = true
        ),
        onMapLongClick = { latLng ->
            mapViewModel.addSafeZone(latLng)
        },
        onMyLocationClick = {
            mapViewModel.currentLocation.value=LatLng(it.latitude, it.longitude)
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(LatLng(it.latitude, it.longitude))
                            .zoom(19f)
                            .tilt(60f)
                            .bearing(180f)
                            .build()
                    ),
                    durationMs = 2000
                )
            }
        },
        onMapLoaded = {
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(LatLng(currentLocation.latitude, currentLocation.longitude))
                            .zoom(18f)
                            .tilt(60f)
                            .bearing(90f)
                            .build()
                    ),
                    durationMs = 1000
                )
            }
        },
    ) {
        var isInAnySafeZone = false
        mapViewModel.safeZones.forEach { safeZone ->
            Marker(
                state = MarkerState(position = safeZone.centerLatlng),
                title = safeZone.name,
                snippet = "Radius: ${safeZone.radius.roundToInt()}meters",
                onInfoWindowLongClick = {
                    mapViewModel.isBottomSheetOpen.value=true
                    mapViewModel.selectedSafeZone.value = safeZone.copy()
                }
            )
            val distance = calculateDistance(mapViewModel.currentLocation.value!!, safeZone.centerLatlng)
            val inSafeZone = (distance - safeZone.radius) <= 0.0
            if (inSafeZone){
                isInAnySafeZone=true
            }
            Circle(
                center = safeZone.centerLatlng,
                radius = safeZone.radius,
                fillColor = if (inSafeZone) Color.Green.copy(alpha = 0.3f) else Color.Blue.copy(alpha = 0.3f),
                strokeColor = Color.Blue,
                strokeWidth = 2f
            )
        }
        mapViewModel.Mydevice_inSafeZone=isInAnySafeZone
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeZoneRadiusDialog(mapViewModel: MapViewModel) {
    AlertDialog(
        onDismissRequest = { mapViewModel.hideSliderDialog(popOut = true) },
        title = { Text("Select Safe Zone Radius and Name") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = mapViewModel.placeTitle.value,
                    onValueChange = { newTitle ->
                        mapViewModel.updatePlaceTitle(newTitle)
                    },
                    label = { Text("Safe Zone Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${mapViewModel.selectedRadius.doubleValue.toInt()} meters",
                    fontSize = 18.sp
                )
                Slider(
                    value = mapViewModel.selectedRadius.value.toFloat(),
                    onValueChange = { value ->
                        mapViewModel.updateRadius(value.toDouble())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Blue)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Place,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                            )
                        }
                    },
                    valueRange = 10f..1000f,
                    steps = 99
                )
            }
        },
        confirmButton = {
            Button(onClick = { mapViewModel.hideSliderDialog(popOut = false) }) {
                Text("Add")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeZoneOptionsBottomSheet(mapViewModel: MapViewModel, safeZone: SafeZone) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display SafeZone name
        Text(
            text = "Update ${safeZone.name}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Slider for radius adjustment
        var radius = remember { mutableStateOf(safeZone.radius.roundToInt()) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "Edit Radius: ${radius.value} meters",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Slider(
                value = radius.value.toFloat(),
                onValueChange = { value ->
                    radius.value = value.toInt()
                    safeZone.radius = value.toDouble()
                    mapViewModel.updateSafeZoneRadius(safeZone)
                },
                onValueChangeFinished = {
                    mapViewModel.updateSafeZoneRadius(safeZone)
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                            .border(2.dp, Color.White, CircleShape) // Optional border for visibility
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "Location Icon", // Added description for accessibility
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp) // Adjust the size of the icon
                                .align(Alignment.Center) // Ensure the icon is centered
                        )
                    }
                },
                valueRange = 10f..1000f,
                steps = 99
            )
        }
        Button(
            onClick = {
                mapViewModel.removeSafeZoneFromFirestore(safeZone)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error // Error color for delete action
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = "Delete Safe Zone",
                color = Color.White
            )
        }
    }
}


fun calculateDistance(geoPoint1: LatLng, geoPoint2: LatLng): Double {
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