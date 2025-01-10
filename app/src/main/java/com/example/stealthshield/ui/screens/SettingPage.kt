package com.example.stealthshield.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.stealthshield.R
import com.example.stealthshield.viewmodel.AuthViewModel

@Composable
fun SettingPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val authProfilePictureUrl = authViewModel.auth.currentUser?.photoUrl
    val isProfilePicturePresent = authProfilePictureUrl != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile picture and welcome text
        ProfileHeader(
            authProfilePictureUrl = authProfilePictureUrl,
            displayName = authViewModel.auth.currentUser?.displayName ?: "User",
            isProfilePicturePresent = isProfilePicturePresent
        )

        Spacer(Modifier.height(16.dp))

        // Profile options list with vector icons
        ProfileOption(
            text = "Emergency Contacts",
            navController = navController,
            route = "emergency_contacts",
            icon = Icons.Default.Phone // Replace with your icon
        )
        ProfileOption(
            text = "Fake Shutdown",
            navController = navController,
            route = "fake_shutdown",
            icon = Icons.Default.Info // Replace with your icon
        )
        ProfileOption(
            text = "Location Tracking",
            navController = navController,
            route = "location_tracking",
            icon = Icons.Default.LocationOn // Replace with your icon
        )
        ProfileOption(
            text = "Manage Safe Zones",
            navController = navController,
            route = "geofence_safe_zones",
            icon = Icons.Default.Lock // Replace with your icon
        )
        ProfileOption(
            text = "Stealth Shield - Features",
            navController = navController,
            route = "feature_screen",
            icon = Icons.Default.Build // Replace with your icon
        )
        ProfileOption(
            text = "Sign Out",
            navController = navController,
            route = "signout",
            icon = Icons.Default.ExitToApp // Replace with your icon
        )
    }
}

@Composable
fun ProfileHeader(authProfilePictureUrl: Any?, displayName: String, isProfilePicturePresent: Boolean) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Profile image with placeholder
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = authProfilePictureUrl ?: R.mipmap.ic_launcher_foreground
                ),
                contentDescription = "User profile picture",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .then(
                        if (isProfilePicturePresent) Modifier else Modifier.scale(1.5f)
                    )
                    .align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Greeting text
        Text(
            text = "StealthShield Settings",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

    }
}

@Composable
fun ProfileOption(
    text: String,
    navController: NavController,
    route: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { navController.navigate(route) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }

}
