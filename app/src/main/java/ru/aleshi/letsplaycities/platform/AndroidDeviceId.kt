package ru.aleshi.letsplaycities.platform

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import ru.aleshi.letsplaycities.base.player.DeviceId

class AndroidDeviceId(context: Context) : DeviceId {

    @SuppressLint("HardwareIds")
    private val _deviceId =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    /**
     * Returns device ID
     */
    override fun getDeviceId(): String = _deviceId

}