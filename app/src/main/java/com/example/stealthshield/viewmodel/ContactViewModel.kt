package com.example.stealthshield.viewmodel

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.twilio.Twilio
//import com.twilio.rest.api.v2010.account.Message
//import com.twilio.type.PhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactViewModel : ViewModel() {
    private val _selectedContacts = MutableStateFlow<List<Contact>>(emptyList())
    val selectedContacts: StateFlow<List<Contact>> = _selectedContacts

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadContactsFromFirebase()
    }

    // Load contacts from Firebase Firestore when the app starts
    private fun loadContactsFromFirebase() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    val contactsSnapshot = firestore.collection("users")
                        .document(user.uid)
                        .collection("emergency_contacts")
                        .get()
                        .await()

                    val contacts = contactsSnapshot.documents.mapNotNull { document ->
                        document.toObject(Contact::class.java)
                    }
                    _selectedContacts.value = contacts
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun addContact(contact: Contact) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    val validPhotoUri = contact.photoUri?.takeIf { it.isNotEmpty() }

                    val contactRef = firestore.collection("users")
                        .document(user.uid)
                        .collection("emergency_contacts")
                        .document()
                    contactRef.set(contact.copy(photoUri = validPhotoUri)).await()

                    _selectedContacts.value = _selectedContacts.value + contact
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    // Remove contact with confirmation from Firestore and state
    fun removeContact(contact: Contact) {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    // Query Firestore to find the contact document
                    val contactCollectionRef = firestore.collection("users")
                        .document(user.uid)
                        .collection("emergency_contacts")

                    val querySnapshot = contactCollectionRef
                        .whereEqualTo("number", contact.number)
                        .get()
                        .await()

                    // Delete the contact from Firestore
                    for (document in querySnapshot.documents) {
                        document.reference.delete().await()
                    }
                    _selectedContacts.value = _selectedContacts.value - contact
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }


    fun sendSmsToAllContactsFormScreen(message: String) {
        Log.d("AlertForStollen", "Contact viewModel body....")
        val smsManager = SmsManager.getDefault()
        Log.d("AlertForStollen", "sms manager getting defaults.... it has ${_selectedContacts.value.size}")

        _selectedContacts.value.forEach { contact ->
            try {
                Log.d("AlertForStollen", "Sending sms for ${contact.name}....")
                smsManager.sendTextMessage(contact.number, null, message, null, null)
                Log.d("AlertForStollen", "sms sent")
            } catch (e: Exception) {
                Log.d("AlertForStollen", "sendSmsToAllContacts: ${e.message}")
            }
        }
        Log.d("AlertForStollen", "sendSmsToAllContacts: Done")
    }




}





@SuppressLint("Range")
fun getContactFromUri(contentResolver: ContentResolver, uri: Uri): Contact? {
    var name = ""
    var number = ""
    var photoUri: Uri? = null

    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
            val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val hasPhoneNumber = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

            photoUri = it.getString(it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))?.let { Uri.parse(it) }

            if (hasPhoneNumber > 0) {
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )
                phoneCursor?.use { pc ->
                    if (pc.moveToFirst()) {
                        number = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                }
            }
        }
    }
    return if (name.isNotEmpty() && number.isNotEmpty()) Contact(name, number, photoUri.toString()) else null
}




data class Contact(
    val name: String = "",
    val number: String = "",
    val photoUri: String? = null
)