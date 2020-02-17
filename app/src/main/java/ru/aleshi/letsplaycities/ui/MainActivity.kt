package ru.aleshi.letsplaycities.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import ru.aleshi.letsplaycities.base.ThemeManager
import ru.aleshi.letsplaycities.social.Google
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasAndroidInjector {

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private val mFriendRequestReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) =
            checkForFirebaseNotifications(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(
            this,
            findNavController(R.id.main_nav_fragment)
        )
        MobileAds.initialize(this)
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        checkForFirebaseNotifications(intent)
        buildApiClient()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mFriendRequestReceiver, IntentFilter("fm_request"))
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

    private fun buildApiClient() {
//        val google = (ServiceType.GL.network as Google)
//        google.signIn(this)
    }


    private fun checkForFirebaseNotifications(intent: Intent) {
        intent.extras?.let { data ->
            if ("fm_request" == data.getString("action", ""))
                startFriendModeGame(data)
        }
    }

    private fun startFriendModeGame(data: Bundle) {
        findNavController(R.id.main_nav_fragment).navigate(
            R.id.globalStartFriendRequestDialog,
            data
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!SocialNetworkManager.onActivityResult(this, requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data)
    }
}
