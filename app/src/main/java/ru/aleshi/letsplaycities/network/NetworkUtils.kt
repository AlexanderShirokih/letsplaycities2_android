package ru.aleshi.letsplaycities.network

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient
import ru.aleshi.letsplaycities.ui.network.LogInListener

object NetworkUtils {

    fun updateToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(LogInListener::class.java.simpleName, "getInstanceId failed", task.exception)
            } else {
                //174 chars
                NetworkClient.getNetworkClient()?.sendFirebaseToken(task.result!!.token)
            }
        }
    }

}