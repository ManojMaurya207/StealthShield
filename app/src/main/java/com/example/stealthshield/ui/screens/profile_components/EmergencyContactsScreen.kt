package com.example.stealthshield.ui.screens.profile_components

import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.stealthshield.R
import com.example.stealthshield.ui.components.AnimatedImageView
import com.example.stealthshield.viewmodel.Contact
import com.example.stealthshield.viewmodel.ContactViewModel
import com.example.stealthshield.viewmodel.getContactFromUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EmergencyContactsScreen() {
    val context = LocalContext.current
    val contactViewModel: ContactViewModel = viewModel()

    val contacts by contactViewModel.selectedContacts.collectAsState()
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var contactToRemove by remember { mutableStateOf<Contact?>(null) }

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val contact = getContactFromUri(context.contentResolver, it)
            contact?.let {
                if (contacts.any { existingContact -> existingContact.number == contact.number }) {
                    Toast.makeText(context, "Contact already added", Toast.LENGTH_SHORT).show()
                } else {
                    contactViewModel.addContact(contact)
                }
            }
        }
    }

    val permissions = listOf(
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.SEND_SMS
    )

    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        } else {
//            Toast.makeText(context, "All permissions are granted", Toast.LENGTH_SHORT).show()
        }
    }

    if (multiplePermissionsState.shouldShowRationale) {
        Toast.makeText(context, "Permissions are needed to proceed. Please grant them.", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .padding(bottom = 16.dp, top = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emergency Contacts",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "The contacts below will be alerted when a fake shutdown occurs.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
        )

        Divider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (contacts.isEmpty()) {
                item {
                    Spacer(modifier = Modifier
                        .weight(1f)
                        .height(200.dp))
                    AnimatedImageView(
                        rawResId = R.raw.empty_contact,
                        modifier = Modifier.size(200.dp),
                        iterations = 1
                    )
                    Text(
                        text = "No contacts added yet",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                item { Spacer(modifier = Modifier
                    .weight(1f)
                    .height(20.dp)) }

                items(contacts) { contact ->
                    val delete = SwipeAction(
                        icon = rememberVectorPainter(Icons.Rounded.Delete),
                        background = Color.Red,
                        onSwipe = {
                            contactToRemove = contact
                            showConfirmationDialog = true
                        }
                    )
                    SwipeableActionsBox(
                        endActions = listOf(delete),
                        swipeThreshold = 100.dp,
                    ) {
                        ContactCard(
                            contact = contact,
                            onRemoveClick = {
                                contactToRemove = contact
                                showConfirmationDialog = true
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = { pickContactLauncher.launch()
            },
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, Color.Blue, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add Contact",
                modifier = Modifier.scale(1.7f)
            )
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Remove Contact") },
                text = { Text("Are you sure you want to remove ${contactToRemove?.name}?") },
                confirmButton = {
                    TextButton(onClick = {
                        contactToRemove?.let { contactViewModel.removeContact(it) }
                        showConfirmationDialog = false
                    }) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}




@Composable
fun ContactCard(
    contact: Contact,
    onRemoveClick: () -> Unit
) {
    val painter = rememberImagePainter(
        data = contact.photoUri,
        builder = {
            error(R.drawable.profile_picture) // Fallback to default contact image
            placeholder(R.drawable.profile_picture)
        }
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (contact.photoUri == "null") {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Default Contact Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(1.dp, Color.Gray, CircleShape)
                )
            } else {
                Image(
                    painter = painter,
                    contentDescription = "Contact Profile",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(1.dp, Color.Gray, CircleShape)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(text = contact.name)
                Text(text = contact.number)
            }
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .scale(0.8f)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .border(1.dp, Color.Red, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete Contact",
                )
            }
        }
    }
}

