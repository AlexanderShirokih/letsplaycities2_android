package ru.aleshi.letsplaycities

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val KEY_ACTION = "action"
        const val KEY_USER_ID = "user_id"
        const val KEY_LOGIN = "login"
        const val KEY_RESULT = "result"
        const val ACTION_FM = "fm_request"
        const val ACTION_FRIEND_REQUEST = "friend_request"
        const val ACTION_FIREBASE = "firebase_action"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let {
            when (it[KEY_ACTION]) {
                ACTION_FM -> LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(ACTION_FIREBASE).apply {
                        putExtra(KEY_ACTION, it[KEY_ACTION])
                        putExtra(KEY_LOGIN, it[KEY_LOGIN])
                        putExtra(KEY_USER_ID, it[KEY_USER_ID]!!.toInt())
                    })
                ACTION_FRIEND_REQUEST -> LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(ACTION_FIREBASE).apply {
                        putExtra(KEY_ACTION, it[KEY_ACTION])
                        putExtra(KEY_LOGIN, it[KEY_LOGIN])
                        putExtra(KEY_RESULT, it[KEY_RESULT])
                        putExtra(KEY_USER_ID, it[KEY_USER_ID]!!.toInt())
                    })
                else -> Unit
            }
        }
    }

}