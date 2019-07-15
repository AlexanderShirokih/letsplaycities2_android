package ru.aleshi.letsplaycities.base.player

import com.google.firebase.iid.FirebaseInstanceId

class DeviceId {
    private var deviceId: String = FirebaseInstanceId.getInstance().id

    override fun toString(): String {
        return deviceId
    }
}