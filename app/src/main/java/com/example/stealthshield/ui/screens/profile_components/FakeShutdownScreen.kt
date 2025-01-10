package com.example.stealthshield.ui.screens.profile_components

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stealthshield.R
import com.example.stealthshield.fakeshutdown.utility.Constants
import com.example.stealthshield.viewmodel.FakeShutdownViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FakeShutdownScreen(
    context: Context,
    fakeShutdownViewModel: FakeShutdownViewModel,
) {
    val fPowerOffEnabled by fakeShutdownViewModel.fPowerOffEnabled.collectAsStateWithLifecycle()
    val dndModeEnabled by fakeShutdownViewModel.dndModeEnabled.collectAsStateWithLifecycle()
    val lockDeviceEnabled by fakeShutdownViewModel.lockDeviceEnabled.collectAsStateWithLifecycle()
    val dialogCloseTriggerSequence by fakeShutdownViewModel.dialogCloseTriggerSequence.collectAsStateWithLifecycle()
//    val detectPackageName by fakeShutdownViewModel.detectPackageName.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    val internalFieldSpacing = 8.dp
    val interFieldSpacing = 16.dp

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(interFieldSpacing)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Fake ShutDown",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(interFieldSpacing))

            SettingSwitchItem(
                title = stringResource(R.string.placeholder_enable_fake_power_off),
                description = stringResource(R.string.setting_desc_enable_fake_power_off),
                isChecked = fPowerOffEnabled.toBoolean(),
                onCheckedChange = {
                    if (fakeShutdownViewModel.getAccessibilityPermission(context)) {
                        fakeShutdownViewModel.setFPowerOffEnabled(it)
                        fakeShutdownViewModel.saveFPowerOffEnabled()
                        Toast.makeText(context, "Fake Power Off is ${if (it) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(internalFieldSpacing))
            SettingSwitchItem(
                title = stringResource(R.string.placeholder_dnd_mode_enabled),
                description = stringResource(R.string.setting_desc_enable_dnd_mode),
                isChecked = dndModeEnabled.toBoolean(),
                onCheckedChange = {
                    if (fakeShutdownViewModel.getDNDPermission(context)) {
                        fakeShutdownViewModel.setDNDModeEnabled(it.toString())
                        fakeShutdownViewModel.saveDNDModeEnabled()
                        Toast.makeText(context, "DND Mode is ${if (it) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(internalFieldSpacing))
            SettingSwitchItem(
                title = stringResource(R.string.placeholder_lock_device_enabled),
                description = stringResource(R.string.setting_desc_enable_lock_device),
                isChecked = lockDeviceEnabled.toBoolean(),
                onCheckedChange = {
                    fakeShutdownViewModel.setLockDeviceEnabled(it.toString())
                    fakeShutdownViewModel.saveLockDeviceEnabled()
                    Toast.makeText(context, "Lock Device is ${if (it) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                }
            )


            Spacer(modifier = Modifier.height(interFieldSpacing))
            val isValidTriggerInput: (String) -> Boolean = { input ->
                input.all { it == 'U' || it == 'D' }
            }

            TextField(
                value = dialogCloseTriggerSequence,
                onValueChange = { if(isValidTriggerInput(it)) {fakeShutdownViewModel.setDialogCloseTriggerSequence(it)} },
                label = { Text(stringResource(id = R.string.placeholder_dialog_close_trigger_sequence)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (!it.isFocused) fakeShutdownViewModel.saveDialogCloseTriggerSequence() }
            )
            Text(
                text =  stringResource(id = R.string.setting_desc_power_off_dismiss_sequence) +
                        " Default is: ${Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(interFieldSpacing))

        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(2f)
            )
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp), // Adjust font size here
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}
