package ru.aleshi.letsplaycities.ui.remote

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_multiplayer.*
import kotlinx.android.synthetic.main.fragment_multiplayer.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.remote.internal.LPSServer
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.utils.NetworkUtil
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class MultiplayerFragment : Fragment(R.layout.fragment_multiplayer) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as MainActivity
        activity.setToolbarVisibility(true)
        view.create.setOnClickListener {
            if (!NetworkUtil.isHotspotRunning(requireContext())) {
                startActivity(Intent(Intent.ACTION_MAIN, null)
                    .apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        component = ComponentName(
                            "com.android.settings",
                            "com.android.settings.TetherSettings"
                        )
                    })
            } else
                findNavController().navigate(R.id.action_multiplayerFragment_to_waitingForDevicesFragment)
        }
        view.connect.setOnClickListener {
            val context = requireContext()
            if (!NetworkUtil.isWifiEnabled(context)) {
                startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
            } else if (NetworkUtil.isWifiIPAddressValid(context)) {
                //TODO: Show WaitingForHostsDialog
                findNavController().navigate(
                    MultiplayerFragmentDirections.showRemoteNetworkFragment(
                        LPSServer.LOCAL_NETWORK_IP,
                        LPSServer.LOCAL_PORT
                    )
                )
            } else {
                Snackbar.make(view, R.string.connect_to_wifi, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lpsApplication.gamePreferences.isLoggedFromAnySN().run {
            create.isEnabled = this
            connect.isEnabled = this
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_SETTINGS
                )
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_SETTINGS),
                    121
                )
            }
    }

}