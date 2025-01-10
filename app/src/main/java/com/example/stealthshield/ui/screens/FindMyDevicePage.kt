package com.example.stealthshield.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.stealthshield.R
import com.example.stealthshield.viewmodel.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID


@Composable
fun FindMyDevicePage(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4223, -122.0848), 12f)
    }
    val currentLocation = mapViewModel.currentLocation

    // Permission handling
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


    LaunchedEffect(currentLocation) {
        mapViewModel.currentLocation.value?.let { latLng ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 13f)
            isLoading = false
        }
    }

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
            coroutineScope = coroutineScope,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Find My Device",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasLocationPermission) {
            Box (
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ){
                MapView(
                    cameraPositionState = cameraPositionState,
                    mapViewModel = mapViewModel,
                    coroutineScope = coroutineScope,
                )
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    predictions = predictions,
                    onPredictionClick = onPredictionClick,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange
                )

                Column(
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    // Center button
                    IconButton(onClick = {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder()
                                        .target(LatLng(mapViewModel.currentLocation.value!!.latitude, mapViewModel.currentLocation.value!!.longitude))
                                        .zoom(19f)
                                        .tilt(60f)
                                        .bearing(180f)
                                        .build()
                                ),
                                durationMs = 2000
                            )
                        }
                    },
                        modifier = Modifier
                            .padding(bottom = 30.dp, end = 5.dp)
                            .size(55.dp)
                            .clip(CircleShape)
                            .background(color = Color.White)
                            .border(1.dp, Color.Red, CircleShape)

                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.re_center,),
                            contentDescription = "User profile picture",
                            modifier = Modifier
                                .scale(0.8f)
                        )
                    }


                    // Bottom sheet Show device button
                    IconButton(onClick = {
                        mapViewModel.isBottomSheetOpen.value=true
                    },
                        modifier = Modifier
                            .padding(bottom = 15.dp, end = 5.dp)
                            .size(55.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = Color.White)
                            .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.current_device_marker,),
                            contentDescription = "Show Devices",
                            modifier = Modifier
                                .scale(0.8f)
                        )
                    }
                }




                if (mapViewModel.isBottomSheetOpen.value) {
                    ModalBottomSheet(
                        sheetState = bottomSheetState,
                        onDismissRequest = {
                            mapViewModel.setShowBottomSheet(false)
                        },
                    ) 
                    {
                        DevicesBottomSheetContent(mapViewModel = mapViewModel,coroutineScope = coroutineScope,cameraPositionState = cameraPositionState)
                    }

                }


            }

        } else {
            Text(text = "Waiting for location permission...", fontSize = 16.sp)
            CircularProgressIndicator()
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
                .menuAnchor()
                .padding(8.dp),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@SuppressLint("HardwareIds", "CoroutineCreationDuringComposition")
@Composable
fun MapView(
    cameraPositionState: CameraPositionState,
    mapViewModel: MapViewModel,
    coroutineScope: CoroutineScope,
) {
    val context = LocalContext.current
    val current_deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        ?: UUID.randomUUID().toString()
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
            zoomControlsEnabled = false,
            tiltGesturesEnabled = true
        ),
        onMyLocationClick = {
            mapViewModel.currentLocation.value = LatLng(it.latitude, it.longitude)
        },
        onMapLoaded = {
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(LatLng(currentLocation.latitude, currentLocation.longitude))
                            .zoom(18f)
                            .tilt(80f)
                            .bearing(90f)
                            .build()
                    ),
                    durationMs = 2000
                )
            }
        },
    ) {
        val bitmapDescriptor = remember {
            bitmapDescriptorFromVector(context, R.drawable.current_device_marker,0.1f)
        }
        mapViewModel.Devices.forEach { device ->
            if (current_deviceId == device.deviceId){
                Marker(
//                    icon = bitmapDescriptor!!,
                    state = MarkerState(position = currentLocation,),
                    title = "Current Device: ${device.device_brand}",
                    snippet = device.deviceName,
                    onClick = {
                        it.showInfoWindow()
                        true
                    }
                )
            }
            else{
                val markerState = rememberMarkerState(position = device.device_location)
                Marker(
                    state = markerState,
                    title = device.device_brand,
                    snippet = device.deviceName,
                    onClick = {
                        it.showInfoWindow()
                        true
                    }
                )
            }

        }
    }
}

@SuppressLint("HardwareIds")
@Composable
fun DevicesBottomSheetContent(
    mapViewModel: MapViewModel,
    cameraPositionState: CameraPositionState,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    val currentDeviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        ?: UUID.randomUUID().toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Connected Devices",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Tap on a device to view its location",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
        )
        mapViewModel.Devices.forEach { device ->
            Card(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.builder()
                                    .target(device.device_location)
                                    .zoom(18f)
                                    .tilt(60f)
                                    .bearing(90f)
                                    .build()
                            ),
                            durationMs = 3000
                        )
                    }
                    mapViewModel.isBottomSheetOpen.value = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFe0f7fa), Color(0xFF80deea))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Device Image/Icon with enhanced design
                    Image(
                        painter = painterResource(id = R.drawable.device_img),
                        contentDescription = "Device Icon",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White, Color.LightGray)
                                )
                            )
                            .border(2.dp, Color.Blue, CircleShape)
                            .padding(4.dp),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = device.device_brand,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = device.deviceName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add an animated online indicator
                    if (currentDeviceId == device.deviceId) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(12.dp)
                                .background(Color.Green, shape = CircleShape)
                                .animateContentSize()
                        )
                    }
                }
            }
        }
    }
}


fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, scaleFactor: Float = 0.5f): BitmapDescriptor? {
    val vectorDrawable: Drawable? = ResourcesCompat.getDrawable(context.resources, vectorResId, null)
    vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    // Create a bitmap from the drawable
    val bitmap = Bitmap.createBitmap(
        vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)

    // Scale down the bitmap
    val matrix = Matrix()
    matrix.postScale(scaleFactor, scaleFactor)
    val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}