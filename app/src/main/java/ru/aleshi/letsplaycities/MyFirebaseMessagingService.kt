package ru.aleshi.letsplaycities

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val KEY_ACTION = "action"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LOGIN = "login"
        private const val ACTION_FM = "fm_request"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let {
            if (ACTION_FM == it[KEY_ACTION]) {
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(it[KEY_ACTION]).apply {
                        putExtra(KEY_ACTION, it[KEY_ACTION])
                        putExtra(KEY_LOGIN, it[KEY_LOGIN])
                        putExtra(KEY_USER_ID, it[KEY_USER_ID])
                    })
            }
        }
    }

}