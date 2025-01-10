package com.example.stealthshield.ui.screens.profile_components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FeatureScreen(modifier: Modifier = Modifier, navController: NavController) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "StealthShield Features",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            FeatureCard(
                title = "Real-Time Tracking",
                description = "Track your device's current location in real-time.",
                icon = Icons.Default.LocationOn,
                onClick = { }
            )
        }

        item {
            FeatureCard(
                title = "Safe Zone Geofencing",
                description = "Manage your safe zones to avoid false alarms.",
                icon = Icons.Default.Lock,
                onClick = { }
            )
        }

        item {
            FeatureCard(
                title = "Emergency Contacts",
                description = "View and manage your emergency contacts.",
                icon = Icons.Default.Phone,
                onClick = { }
            )
        }

        item {
            FeatureCard(
                title = "Fake Shutdown",
                description = "Enable fake shutdown to trap thieves.",
                icon = Icons.Default.Info,
                onClick = { }
            )
        }

        item {
            Divider()
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Experimental Features..",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            FeatureCard(
                title = "Front Camera Capture",
                description = "Automatically capture images when unauthorized access is detected.",
                icon = Icons.Default.CheckCircle,
                onClick = { navController.navigate("victim_capture") }
            )
        }

        item {
            FeatureCard(
                title = "Location History",
                description = "Review the location history of your device.",
                icon = Icons.Default.Search,
                onClick = { }
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)) // Subtle background
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp) // Adjusted icon size
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface // Improved color scheme
                )
                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
