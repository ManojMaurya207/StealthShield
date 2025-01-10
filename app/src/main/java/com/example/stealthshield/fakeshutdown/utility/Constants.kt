package com.example.stealthshield.fakeshutdown.utility


object Constants {
    // Build Constants
    const val APP_ID = "StealthShield"

    //Device Info
    const val DEVICE_ID = "device_id"

    // Package Constants
    const val SYSTEM_UI_PACKAGE = "com.android.systemui"

    // Shared Preferences Constants
    const val APP_PREFS = "${APP_ID}_shared_prefs"
    const val F_POWER_OFF_ENABLED_KEY = "f_power_off_enabled"
    const val DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY = "dialog_close_trigger_sequence"
    const val DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT = "UUDD"
    const val DND_MODE_ENABLED_KEY = "dnd_mode_enabled"
    const val LOCK_DEVICE_ENABLED_KEY = "lock_device_enabled"
    const val DETECT_PACKAGE_NAME_KEY = "detect_package_name"
    const val DETECT_PACKAGE_NAME_DEFAULT = SYSTEM_UI_PACKAGE
    const val DETECT_KEYWORDS_KEY = "detect_keywords"
    const val DETECT_KEYWORDS_DEFAULT = "power off, restart, emergency, poweroff, shutdown, shut down"

    // Permission Constants
    enum class SpecialPermission(val value: String) {
        ACCESSIBILITY("Accessibility"),
        DND("Do Not Disturb"),
    }

}