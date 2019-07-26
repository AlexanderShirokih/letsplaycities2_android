package ru.aleshi.letsplaycities.base.player

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings

class DeviceId(activity: Activity) {

    @SuppressLint("HardwareIds")
    private val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)

    override fun toString(): String {
        return deviceId
    }
}