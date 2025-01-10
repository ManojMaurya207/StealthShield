package com.example.stealthshield.ui.screens.profile_components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.stealthshield.viewmodel.AuthViewModel
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VictimCaptureScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }

    // ActivityResultLauncher for camera permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        permissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "Camera permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Check for camera permission
    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Victim Capture",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (permissionGranted) {
                // Open Camera
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(context.packageManager) != null) {
                    val photoFile = createImageFile(context) // Create a file to save the image
                    imageUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    context.startActivity(intent)
                    Toast.makeText(context, "Camera opened. Please take a photo.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No camera app found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please grant camera permission first.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Capture Victim Photo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            imageUri?.let {
                uploadImageToFirebase(it, context)
            } ?: run {
                Toast.makeText(context, "No image captured to upload.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Upload to Firebase")
        }
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_$timeStamp", ".jpg", storageDir)
}

private fun uploadImageToFirebase(imageUri: Uri, context: Context) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val imageRef = storageRef.child("images/${imageUri.lastPathSegment}")

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            Log.d("Upload", "Image uploaded successfully")
            Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { exception ->
            Log.e("Upload", "Error uploading image", exception)
            Toast.makeText(context, "Error uploading image: ${exception.message}", Toast.LENGTH_LONG).show()
        }
}
