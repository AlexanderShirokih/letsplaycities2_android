package ru.aleshi.letsplaycities.ui.network

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.fragment_network.*
import org.json.JSONObject
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.social.*
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkListener
import java.io.File


class NetworkFragment : Fragment() {

    private lateinit var mApplication: LPSApplication
    private lateinit var mGamePreferences: GamePreferences
    private lateinit var mAvatarModelView: AvatarViewModel

    private val mLoginListener: LogInListener = LogInListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApplication = lpsApplication
        mGamePreferences = mApplication.gamePreferences

        mAvatarModelView = ViewModelProviders.of(requireActivity())[AvatarViewModel::class.java]
        mAvatarModelView.avatarPath.observe(this, Observer {
            if (it == null) {
                Picasso.get()
                    .load(R.drawable.ic_player)
                    .into(roundedImageView)
            } else {
                Picasso.get().isLoggingEnabled = true
                Picasso.get()
                    .load(File(it).toUri())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .error(R.drawable.ic_player)
                    .into(roundedImageView)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as MainActivity
        activity.setToolbarVisibility(true)

        if (mGamePreferences.isChangeModeDialogRequested()) {
            findNavController().navigate(R.id.showChangeModeDialog)
        }

        if (mGamePreferences.isLoggedFromAnySN()) {
            setupWithSN()
        } else {
            setup()
//            checkForRequest()
        }

        btnVk.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.VK))
        btnOk.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.OK))
        btnFb.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.FB))
        btnGl.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.GL))
        btnLogout.setOnClickListener {
            SocialNetworkManager.logout(lpsApplication.gamePreferences)
            setup()
        }

        btnLoginNoSn.setOnClickListener {
            findNavController().navigate(R.id.showLoginDialog)
        }

        btnFriends.setOnClickListener {
            findNavController().navigate(R.id.start_friends_fragment)
        }

        SocialNetworkManager.registerCallback(object : SocialNetworkLoginListener {

            override fun onLoggedIn(info: SocialInfo, accessToken: String) {
                info.saveToPreferences(mApplication.gamePreferences, accessToken)
                setupWithSN()
//                this.accToken = accessToken;
            }

            override fun onError() {
                Snackbar.make(requireView(), R.string.auth_error, Snackbar.LENGTH_LONG).show()
            }

        })
    }

    private fun setup() {

        sn_desc.setText(R.string.networkmode_auth_desc)
        group_sn.visibility = View.VISIBLE
        group_connect.visibility = View.GONE

        mAvatarModelView.avatarPath.value = null

        setAvatarEditable(false)
    }

    private fun setupWithSN() {
        group_sn.visibility = View.GONE
        group_connect.visibility = View.VISIBLE
        sn_desc.text = mApplication.gamePreferences.getLogin()

        setAvatarEditable(mApplication.gamePreferences.isLoggedFromNative())

        mAvatarModelView.avatarPath.value = mGamePreferences.getAvatarPath()
//        checkForRequest()
    }

    private fun setAvatarEditable(isEditable: Boolean) {
        roundedImageView.setOnClickListener(
            if (isEditable)
                View.OnClickListener { findNavController().navigate(R.id.showChangeAvatarDialog) }
            else
                null
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                override fun onResult(res: VKAccessToken) {
                    ServiceType.VK.network.onLoggedIn(requireContext(), res.accessToken)
                }

                override fun onError(error: VKError) {
                    ServiceType.VK.network.onError()
                }

            })) {
            return
        }

        val odnoklassniki = Odnoklassniki.getInstance()

        if (odnoklassniki.onAuthActivityResult(requestCode, resultCode, data, object : OkListener {
                override fun onSuccess(json: JSONObject) {
                    ServiceType.OK.network.onLoggedIn(requireContext(), json.getString("access_token"))
                }

                override fun onError(error: String) {
                    ServiceType.OK.network.onError()
                }
            })) {

            return
        } else if (odnoklassniki.onActivityResultResult(requestCode, resultCode, data, object : OkListener {
                override fun onSuccess(json: JSONObject) {
                    //access_token, secret_key
                }

                override fun onError(error: String) {
                    ServiceType.OK.network.onError()
                }
            })) {
            return
        }

        if ((ServiceType.FB.network as Facebook).callbackManager!!.onActivityResult(requestCode, resultCode, data)) {
            return
        }

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Google.RC_SIGN_IN) {
            (ServiceType.GL.network as Google).onActivityResult(requireActivity(), data)
        }
    }

}