package com.example.stealthshield.fakeshutdown.service.event

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.stealthshield.R
import com.example.stealthshield.fakeshutdown.manager.SharedPreferencesManager
import com.example.stealthshield.fakeshutdown.service.event.DialogCloseEvent.Companion.dialogCloseTriggerSequence
import com.example.stealthshield.fakeshutdown.utility.Constants
import com.example.stealthshield.services.LocationService
import com.example.stealthshield.viewmodel.Contact
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PowerMenuOverrideEvent @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    companion object {
        var fPowerOffEnabled = false
        var dndModeEnabled = false
        var lockDeviceEnabled = false
        var detectPackageName = ""
        var detectKeywords = listOf<String>()


        var emptyDialog: Dialog? = null
    }

    init {
        fPowerOffEnabled = sharedPreferencesManager.get(Constants.F_POWER_OFF_ENABLED_KEY).toBoolean()
        dndModeEnabled = sharedPreferencesManager.get(Constants.DND_MODE_ENABLED_KEY).toBoolean()
        lockDeviceEnabled = sharedPreferencesManager.get(Constants.LOCK_DEVICE_ENABLED_KEY).toBoolean()
        detectPackageName = sharedPreferencesManager.get(Constants.DETECT_PACKAGE_NAME_KEY).toString()
        detectKeywords = sharedPreferencesManager.get(Constants.DETECT_KEYWORDS_KEY)?.split(',')
            ?.map { it.trim() } ?: Constants.DETECT_KEYWORDS_DEFAULT.split(',')
    }

    private var powerMenuOpen = false

    fun checkInternetConnectivity(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }




    @RequiresApi(Build.VERSION_CODES.P)
    fun handlePowerMenuEvent(context: Context, event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {

        if (! fPowerOffEnabled){
            return
        }


        //Fence Management
        var insafezone = sharedPreferencesManager.get("Insafezone").toBoolean()
        var fenceName = sharedPreferencesManager.get("InFence")
        Log.d("PowerMenuOverrideEvent", "handlePowerMenuEvent insafezone value: $insafezone")



        val onDeviceClass = "android.app.Dialog"  //For launching on my mobile

        val packageName = event.packageName?.toString() ?: return
        val className =  event.className?.toString() ?: return
        var phoneOption = ""
        if(packageName==Constants.SYSTEM_UI_PACKAGE) {
            try{
                phoneOption=event.text[0].toString()
            }catch(exception:Exception) {
//                Toast.makeText(context, "Error: $exception", Toast.LENGTH_SHORT).show()
            }
        }

        if (packageName == Constants.SYSTEM_UI_PACKAGE && (phoneOption == "Phone options" || className == onDeviceClass)) {
            try {

                //For fence managed canceling override
                if (insafezone && checkInternetConnectivity(context)){
                    Toast.makeText(context,"Device in fence $fenceName \nContinuing with Default Power Menu",Toast.LENGTH_LONG).show()
                    return
                }


                val parentNodeInfo = event.source ?: return
                val nodeQueue = mutableListOf<AccessibilityNodeInfo>()
                nodeQueue.add(parentNodeInfo)
                Timber.tag("nodeQueue").e("${nodeQueue}   !!")
                while (nodeQueue.isNotEmpty()) {
                    val currentNode = nodeQueue.removeAt(0)
                    for (i in 0 until currentNode.childCount) {
                        nodeQueue.add(currentNode.getChild(i))
                    }
                    val text = currentNode.text?.toString()
                    val tooltipText = currentNode.tooltipText?.toString()
                    val hintText = currentNode.hintText?.toString()
                    val contentDescription = currentNode.contentDescription?.toString()

                    if (!powerMenuOpen && (
                                (tooltipText != null && detectKeywords.any { tooltipText.contains(it, ignoreCase = true) }) ||
                                        (hintText != null && detectKeywords.any { hintText.contains(it, ignoreCase = true) }) ||
                                        (contentDescription != null && detectKeywords.any { contentDescription.contains(it, ignoreCase = true) }) ||
                                        (text != null && detectKeywords.any { text.contains(it, ignoreCase = true)})
                                )){
                        powerMenuOpen = true
                        Timber.d("[${::handlePowerMenuEvent.name}] Detected")

                        performGlobalAction(GLOBAL_ACTION_BACK)
                        val dialog = Dialog(context)
                        dialog.setContentView(R.layout.power_off_menu)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window?.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
                        dialog.setOnDismissListener { powerMenuOpen = false }

                        val powerOffButton = dialog.findViewById<ImageButton>(R.id.btn_power_off)
                        val restartButton = dialog.findViewById<ImageButton>(R.id.btn_restart)
                        val emergencyButton = dialog.findViewById<ImageButton>(R.id.btn_emergency)

                        powerOffButton.setOnClickListener {

                            beginShutdownSequence(context, performGlobalAction)
                            dialog.dismiss()
                        }

                        restartButton.setOnClickListener {
                            beginShutdownSequence(context, performGlobalAction)
                            dialog.dismiss()
                        }

                        emergencyButton.setOnClickListener {
                            beginShutdownSequence(context, performGlobalAction)
                            dialog.dismiss()
                        }

                        dialog.show()
                        break
                    }
                }
            } catch (e: Exception) {
                Timber.e("[${::handlePowerMenuEvent.name}] Error: $e")
            }
        }
    }







    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    //For SMS Alerts loading the contact form firestore
    @OptIn(DelicateCoroutinesApi::class)
    fun loadEmergencyContacts(onContactsLoaded: (List<Contact>) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()

        if (user != null) {
            GlobalScope.launch {
                try {

                    val contactsSnapshot = firestore.collection("users")
                        .document(user.uid)
                        .collection("emergency_contacts")
                        .get()
                        .await()

                    val contacts = contactsSnapshot.documents.mapNotNull { document ->
                        document.toObject(Contact::class.java)
                    }
                    Log.d("AlertForStollen","Fetched " + contacts.size + " contacts from Firebase.")
                    onContactsLoaded(contacts)
                } catch (e: Exception) {
                    Log.d("AlertForStollen","Error fetching contacts: " + e.message)
                }
            }
        } else {
            Log.d("AlertForStollen","User is not authenticated!")
        }
    }


    fun sendSMSToEmergencyContacts(lastKnownLocation: String, dateTime: String) {
        try {
            val smsManager = SmsManager.getDefault()
            val messageBody = "StealthShield Alert: \nMy device was stolen. \nLast Location: $lastKnownLocation at $dateTime. \nPlease assist and notify authorities. \nThank you!"

            loadEmergencyContacts { contacts ->
                contacts.forEach { contact ->
                    try {
                        smsManager.sendTextMessage(
                            contact.number, // Send to one recipient at a time
                            null,
                            messageBody,
                            null,
                            null
                        )
                        Log.d("AlertForStollen", "SMS sent to: ${contact.number}")
                    } catch (e: Exception) {
                        Log.e("AlertForStollen", "Failed to send SMS to: ${contact.number}, Error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AlertForStollen", "Error sending SMS: ${e.message}")
        }
    }




    @RequiresApi(Build.VERSION_CODES.P)
    private fun beginShutdownSequence(context: Context, performGlobalAction: (Int) -> Unit) {

        // Triggering service to start location tracking and sending location Alert
        val intent =Intent(context, LocationService::class.java)
        context.startForegroundService(intent)

        //Sending SMS to SOS contact
        try {
            sendSMSToEmergencyContacts("Churchgate ", getCurrentDateTime())
        }
        catch (e :Exception){
            Log.d("AlertForStollen","AlertForStollen: " + e)
        }

        val showShutdownDialog = showShutdownDialog(context)
        turnOnDNDMode(context)
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000L)
            vibrateDevice(context)
            lockDevice(performGlobalAction)
            if(dialogCloseTriggerSequence.isNullOrBlank()){
                dialogCloseTriggerSequence = Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT
            }
            emptyDialog = showEmptyDialog(context)
            delay(2000L)
            showShutdownDialog.dismiss()
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun lockDevice(performGlobalAction: (Int) -> Unit){
        if(lockDeviceEnabled){
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showEmptyDialog(context: Context) : Dialog {
        val emptyDialog = Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)

        // Make the dialog full screen
        emptyDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        emptyDialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        emptyDialog.window?.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        emptyDialog.window?.setBackgroundDrawableResource(android.R.color.black)
        emptyDialog.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Disable outside touch to dismiss
        emptyDialog.setCancelable(false)
        emptyDialog.setCanceledOnTouchOutside(false)

        emptyDialog.show()
        return emptyDialog
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showShutdownDialog(context: Context) : Dialog {
        // TODO: Make different dialog styles? Or maybe just choose from screenshot a static picture.
        val shutdownDialog = Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        shutdownDialog.setContentView(R.layout.shutdown_dialog)

        // Make the dialog full screen
        shutdownDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        shutdownDialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        shutdownDialog.window?.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        shutdownDialog.window?.setBackgroundDrawableResource(android.R.color.black)
        shutdownDialog.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Disable outside touch to dismiss
        shutdownDialog.setCancelable(false)
        shutdownDialog.setCanceledOnTouchOutside(false)

        shutdownDialog.show()
        return shutdownDialog
    }

    private fun turnOnDNDMode(context: Context) {
        if(dndModeEnabled){
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrateDevice(context: Context) {
        val vibrationDuration = 700L
        val vibrationIntensity = 255
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, vibrationIntensity))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, vibrationIntensity))
            }
        }
    }

}