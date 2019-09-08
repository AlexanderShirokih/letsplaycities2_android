package ru.aleshi.letsplaycities.utils

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager

object NetworkUtil {

    private fun getWifiManager(context: Context): WifiManager {
        return context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    fun isWifiEnabled(context: Context): Boolean {
        return getWifiManager(context).isWifiEnabled
    }

    fun isWifiIPAddressValid(context: Context): Boolean {
        return getWifiManager(context).connectionInfo.ipAddress != 0
    }

    fun isHotspotRunning(context: Context): Boolean {
        val wifi = getWifiManager(context)
        for (method in wifi.javaClass.declaredMethods) {
            if (method.name == "isWifiApEnabled")
                try {
                    method.isAccessible = true
                    return method.invoke(wifi) as Boolean
                } catch (e: Exception) {
                    e.printStackTrace()
                }

        }
        return false
    }

}