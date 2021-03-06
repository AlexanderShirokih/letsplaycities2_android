package ru.aleshi.letsplaycities.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.GoogleApiAvailability
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_main.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.service.MyFirebaseMessagingService
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.utils.GoogleServicesExt.await
import ru.aleshi.letsplaycities.utils.Utils
import javax.inject.Inject

class MainActivity : BaseAsyncActivity(), HasAndroidInjector {

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var prefs: GamePreferences

    private val mFriendRequestReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) =
            checkForFirebaseNotifications(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        Utils.applyTheme(prefs, this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(
            this,
            findNavController(R.id.main_nav_fragment)
        )
        MobileAds.initialize(this)
        checkForFirebaseNotifications(intent)
        lifecycleScope.launchWhenStarted {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this@MainActivity)
                .await()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                mFriendRequestReceiver,
                IntentFilter(MyFirebaseMessagingService.ACTION_FIREBASE)
            )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(mFriendRequestReceiver)
        super.onPause()
    }


    override fun onSupportNavigateUp() = findNavController(R.id.main_nav_fragment).navigateUp()

    fun setToolbarVisibility(visible: Boolean) {
        if (visible)
            supportActionBar?.show()
        else
            supportActionBar?.hide()
    }

    private fun checkForFirebaseNotifications(intent: Intent) {
        intent.extras?.let { data ->
            when (data.getString(MyFirebaseMessagingService.KEY_ACTION)) {
                MyFirebaseMessagingService.ACTION_FM -> startFriendModeGame(
                    translateStringKey(
                        data,
                        MyFirebaseMessagingService.KEY_USER_ID,
                        MyFirebaseMessagingService.KEY_TARGET
                    )
                )
                MyFirebaseMessagingService.ACTION_FRIEND_REQUEST -> showFriendRequestDialog(
                    translateStringKey(data, MyFirebaseMessagingService.KEY_USER_ID)
                )
            }
        }
    }

    private fun translateStringKey(data: Bundle, vararg keys: String): Bundle {
        for (key in keys) {
            val value = data.get(key)
            if (value != null && value is String) {
                data.remove(key)
                data.putInt(key, value.toInt())
            }
        }
        return data
    }

    private fun showFriendRequestDialog(data: Bundle) {
        findNavController(R.id.main_nav_fragment).navigate(
            R.id.globalStartFriendRequestDialog,
            data
        )
    }

    private fun startFriendModeGame(data: Bundle) {
        findNavController(R.id.main_nav_fragment).navigate(
            R.id.globalStartFriendGameRequestDialog,
            data
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!SocialNetworkManager.onActivityResult(this, requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data)
    }
}
