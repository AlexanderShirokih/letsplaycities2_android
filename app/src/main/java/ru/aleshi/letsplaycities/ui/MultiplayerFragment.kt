package ru.aleshi.letsplaycities.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_multiplayer.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.remote.LPSServer
import ru.aleshi.letsplaycities.remote.RemoteContract
import ru.aleshi.letsplaycities.utils.NetworkUtil
import javax.inject.Inject

class MultiplayerFragment : Fragment(), RemoteContract.View {

//    @Inject
//    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var remotePresenter: RemoteContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)
        requestPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_multiplayer, container, false)
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
                remotePresenter.onCreateConnection()
        }
        view.connect.setOnClickListener {
            val context = requireContext()
            if (!NetworkUtil.isWifiEnabled(context)) {
                startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
            } else if (NetworkUtil.isWifiIPAddressValid(context)) {
                //TODO: Show WaitingForHostsDialog
                findNavController().navigate(
                    MultiplayerFragmentDirections.showRemoteNetworkFragment(
                        LPSServer.LOCAL_NETWORK_IP
                    )
                )
            } else {
                Snackbar.make(view, R.string.connect_to_wifi, Snackbar.LENGTH_SHORT).show()
            }
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