package com.example.stealthshield.fakeshutdown.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.example.stealthshield.fakeshutdown.service.event.DialogCloseEvent
import com.example.stealthshield.fakeshutdown.service.event.PowerMenuOverrideEvent
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EventListenerService : AccessibilityService() {
    @Inject
    lateinit var powerMenuOverrideEvent: PowerMenuOverrideEvent

    @Inject
    lateinit var dialogCloseEvent: DialogCloseEvent

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                powerMenuOverrideEvent.handlePowerMenuEvent(this, event, ::performGlobalAction)
                Timber.tag("Powerlongpressed").e("${event} You pressed power button !!")
                Log.d("Powerlongpressed", "${event} You pressed power button !!")
            }
            else -> {Timber.w("[${::onAccessibilityEvent.name}]" + " Event type not handled: ${event.eventType}")
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null) {
            dialogCloseEvent.handleTriggerEvent(event)
        }
        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {

    }
}
