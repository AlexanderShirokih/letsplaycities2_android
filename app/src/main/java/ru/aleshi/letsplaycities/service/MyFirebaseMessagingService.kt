package ru.aleshi.letsplaycities.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.quandastudio.lpsclient.core.LpsRepository
import javax.inject.Inject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val KEY_ACTION = "action"
        const val KEY_USER_ID = "user_id"
        const val KEY_LOGIN = "login"
        const val KEY_RESULT = "result"
        const val KEY_TARGET = "target_id"
        const val ACTION_FM = "fm_request"
        const val ACTION_FRIEND_REQUEST = "friend_request"
        const val ACTION_FIREBASE = "firebase_action"
        const val TAG = "FbMessagingService"
    }

    @Inject
    lateinit var lpsRepository: LpsRepository
    @Inject
    lateinit var authDataFactory: GameAuthDataFactory

    private val job = SupervisorJob()

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNewToken(newToken: String) {
        if (authDataFactory.getCredentials().isValid()) {
            val errorHandler = CoroutineExceptionHandler { _, exception ->
                Log.e(TAG, Log.getStackTraceString(exception))
            }
            CoroutineScope(job).launch(errorHandler) {
                lpsRepository.updateToken(newToken)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let { data ->
            when (data[KEY_ACTION]) {
                ACTION_FM -> LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(
                        Intent(ACTION_FIREBASE)
                            .putExtra(KEY_ACTION, data[KEY_ACTION])
                            .putExtra(KEY_LOGIN, data[KEY_LOGIN])
                            .putExtra(KEY_USER_ID, data[KEY_USER_ID]!!.toInt())
                            .putExtra(KEY_TARGET, data[KEY_TARGET]!!.toInt())
                    )
                ACTION_FRIEND_REQUEST -> LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(
                        Intent(ACTION_FIREBASE)
                            .putExtra(KEY_ACTION, data[KEY_ACTION])
                            .putExtra(KEY_LOGIN, data[KEY_LOGIN])
                            .putExtra(KEY_RESULT, data[KEY_RESULT])
                            .putExtra(KEY_USER_ID, data[KEY_USER_ID]!!.toInt())
                    )
                else -> Unit
            }
        }
    }

}