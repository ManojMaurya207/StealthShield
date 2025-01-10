package com.example.stealthshield.ui.screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stealthshield.R
import com.example.stealthshield.ui.components.AnimatedImageView
import com.example.stealthshield.viewmodel.AuthViewModel
import com.example.stealthshield.viewmodel.HomeViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomePage(
    homeViewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val loggedInDevices by homeViewModel.loggedInDevices.observeAsState(0)
    val totalSafeZones by homeViewModel.totalSafeZones.observeAsState(0)
    val activeSOSContacts by homeViewModel.activeSOSContacts.observeAsState(0)

    LaunchedEffect(Unit) {
        homeViewModel.fetchData()
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated Greeting Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3F5B8E).copy(alpha = 0.3f), // Lighter Navy Blue
                            Color(0xFF3F5B8E) // Lighter Navy Blue
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Greeting text
                Text(
                    text = "Hello, ${authViewModel.auth.currentUser?.displayName ?: "User"}",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Welcome to StealthShield!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Divider()

        InfoCard(
            title = "Logged-In Devices",
            value = loggedInDevices.toString(),
            icon = Icons.Default.Person,
            cardColors = listOf(Color(0xFF001F3F), Color(0xFF2B8BF1)) // Navy Blue to Medium Blue
        )

        InfoCard(
            title = "Total Safe Zones",
            value = totalSafeZones.toString(),
            icon = Icons.Default.Lock,
            cardColors = listOf(Color(0xFF001F3F), Color(0xFF20B2AA)) // Navy Blue to Greenish Blue
        )

        InfoCard(
            title = "Active SOS Contacts",
            value = activeSOSContacts.toString(),
            icon = Icons.Default.Phone,
            cardColors = listOf(Color(0xFF001F3F), Color(0xFF88FFFF)) // Navy Blue to White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Safety Tips Section
        Text(
            text = "Safety Tips",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )

        // Safety Tips List with automatic scrolling
        val tips = listOf(
            "1. Set up emergency contacts for quick alerts.",
            "2. Inform trusted friends or family members about your app's features for better support during emergencies.",
            "3. Add safe zones to prevent false alarms.",
            "4. Test the Fake Shutdown feature periodically to ensure it works as expected.",
            "5. Ensure your device has sufficient battery for the Fake Shutdown feature to activate at all time.",
        )

        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            while (true) {
                delay(10000) // Wait for 5 seconds
                val currentIndex = listState.firstVisibleItemIndex
                val nextIndex = (currentIndex + 1) % tips.size // Loop back to the start
                listState.animateScrollToItem(nextIndex)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tips) { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = tip,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, icon: ImageVector, cardColors: List<Color>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColors[0])
    ) {
        Box {
            Row(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = cardColors
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}