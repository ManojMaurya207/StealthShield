package com.example.stealthshield.fakeshutdown.service.event

import android.view.KeyEvent
import com.example.stealthshield.fakeshutdown.manager.SharedPreferencesManager
import com.example.stealthshield.fakeshutdown.service.event.PowerMenuOverrideEvent.Companion.emptyDialog
import com.example.stealthshield.fakeshutdown.utility.Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT
import com.example.stealthshield.fakeshutdown.utility.Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DialogCloseEvent @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    companion object {
        var dialogCloseTriggerSequence: String? = null
    }

    init {
        dialogCloseTriggerSequence = sharedPreferencesManager
            .get(DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY) ?: DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT
    }

    private var dialogCloseTriggerState = 0
    private var dialogCloseTriggerJob: Job? = null

    fun handleTriggerEvent(event: KeyEvent) {
        if(event.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if(! dialogCloseTriggerSequence.isNullOrBlank()) {
                        handleDialogCloseTriggerEvent(event.keyCode, dialogCloseTriggerSequence !!)
                    }
                }
            }
        }
    }

    // TODO: Convert to Finite Automata Machine
    private fun handleDialogCloseTriggerEvent(keyCode: Int, triggerSequence: String) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && triggerSequence[dialogCloseTriggerState].uppercase() == "U") {
            dialogCloseTriggerState++
            if(dialogCloseTriggerState == triggerSequence.length) {
                dialogCloseTriggerState = 0
                dialogClose()
            }
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && triggerSequence[dialogCloseTriggerState].uppercase() == "D") {
            dialogCloseTriggerState++
            if(dialogCloseTriggerState == triggerSequence.length) {
                dialogCloseTriggerState = 0
                dialogClose()
            }
        } else {
            dialogCloseTriggerState = 0
        }

        // Reset the sequence if no key is pressed within 1 second
        dialogCloseTriggerJob?.cancel()
        dialogCloseTriggerJob = CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            dialogCloseTriggerState = 0
        }
    }

    private fun dialogClose(){
        emptyDialog?.dismiss()
    }
}