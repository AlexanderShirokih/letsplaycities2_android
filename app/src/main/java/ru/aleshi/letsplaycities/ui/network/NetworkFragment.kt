package ru.aleshi.letsplaycities.ui.network

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_network.*
import org.json.JSONObject
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.network.PlayerData
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient
import ru.aleshi.letsplaycities.social.*
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.UiErrorListener
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.RECONNECTION_DELAY_MS
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import ru.ok.android.sdk.Odnoklassniki
import ru.ok.android.sdk.OkListener
import java.io.ByteArrayOutputStream
import java.io.File


class NetworkFragment : Fragment() {

    private lateinit var mApplication: LPSApplication
    private lateinit var mGamePreferences: GamePreferences
    private lateinit var mNetworkViewModel: NetworkViewModel
    private lateinit var mAuthData: AuthData
    private var mLastConnectionTime: Long = 0

    private lateinit var mLoginListener: LogInListener

    private val mNormalConstraintSet: ConstraintSet = ConstraintSet()
    private val mLoadingConstraintSet: ConstraintSet = ConstraintSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApplication = lpsApplication
        mGamePreferences = mApplication.gamePreferences
        mLoginListener = LogInListener(this, mGamePreferences)

        mNetworkViewModel = ViewModelProviders.of(requireActivity())[NetworkViewModel::class.java]
        mNetworkViewModel.run {
            avatarPath.value = mApplication.gamePreferences.getAvatarPath()
            avatarPath.observe(this@NetworkFragment, Observer {
                if (it == null) {
                    roundedImageView.setImageResource(R.drawable.ic_player)
                } else {
                    Utils.loadAvatar(File(it).toUri())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { bitmap ->
                            roundedImageView.setImageBitmap(bitmap)
                        }
                        .subscribe()
                }
            })
            nativeLogin.observe(this@NetworkFragment, Observer {
                (ServiceType.NV.network as NativeAccess).userLogin = it
                SocialNetworkManager.login(ServiceType.NV, requireActivity())
            })
            friendsInfo.observe(this@NetworkFragment, Observer { friendsInfo ->
                if (friendsInfo != null) {
                    createPlayerData { playerData ->
                        startGame(playerData, NetworkClient.PlayState.WAITING, friendsInfo)
                    }
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as MainActivity
        activity.setToolbarVisibility(true)

        mNormalConstraintSet.clone(root)
        mLoadingConstraintSet.clone(activity, R.layout.fragment_network_connecting)

        if (mGamePreferences.isChangeModeDialogRequested()) {
            findNavController().navigate(R.id.showChangeModeDialog)
        }
        if (mGamePreferences.isLoggedFromAnySN()) {
            mAuthData = AuthData.loadFromPreferences(mGamePreferences)
            setupWithSN()
        } else {
//TODO:            checkForRequest()
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
            val extras = FragmentNavigatorExtras(
                roundedImageView to "transition_avatar"
            )
            findNavController().navigate(R.id.showLoginDialog, null, null, extras)
        }

        btnFriends.setOnClickListener {
            findNavController().navigate(R.id.start_friends_fragment)
        }

        btnConnect.setOnClickListener {
            createPlayerData {
                startGame(it, NetworkClient.PlayState.PLAY, null)
            }
        }
        btnCancel.setOnClickListener {
            onCancel()
        }

        SocialNetworkManager.registerCallback(object : SocialNetworkLoginListener {

            override fun onLoggedIn(data: AuthData) {
                mAuthData = data.apply { saveToPreferences(mGamePreferences) }
                setupWithSN()
            }

            override fun onError() {
                Snackbar.make(requireView(), R.string.auth_error, Snackbar.LENGTH_LONG).show()
            }

        })
    }

    internal fun onCancel() {
        setLoadingLayout(false)
        NetworkClient.getNetworkClient()?.run {
            disconnect()
        }
    }

    private fun setup() {
        sn_desc.setText(R.string.networkmode_auth_desc)
        group_sn.visibility = View.VISIBLE
        group_connect.visibility = View.GONE

        mNetworkViewModel.avatarPath.value = null
    }

    private fun setupWithSN() {
        group_sn.visibility = View.GONE
        group_connect.visibility = View.VISIBLE
        sn_desc.text = mApplication.gamePreferences.getLogin()
//        checkForRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                override fun onResult(res: VKAccessToken) {
                    ServiceType.VK.network.onLoggedIn(requireActivity(), res.accessToken)
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
                    ServiceType.OK.network.onLoggedIn(requireActivity(), json.getString("access_token"))
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

    private fun createPlayerData(callback: (playerData: PlayerData) -> Unit) {
        val context = requireContext()
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val userData = PlayerData.create(mAuthData.login).apply {
            setBuildInfo(pInfo.versionName, PackageInfoCompat.getLongVersionCode(pInfo).toInt())
            authData = mAuthData
            canReceiveMessages = mGamePreferences.canReceiveMessages()
        }

        val path = mGamePreferences.getAvatarPath()
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                Utils.loadAvatar(file.toUri())
                    .doOnNext { bitmap ->
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                        userData.avatar = stream.toByteArray()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        callback(userData)
                    }
                    .subscribe()
            } else
                callback(userData)
        } else callback(userData)
    }

    internal fun startGame(
        userData: PlayerData,
        state: NetworkClient.PlayState,
        friendsInfo: FriendsInfo?
    ) {
        setLoadingLayout(true)
        val networkClient = NetworkClient.createNetworkClient(UiErrorListener(this) {
            onCancel()
        })
        checkForWaiting {
            networkClient.connect(mLoginListener, userData, state, friendsInfo?.userId)
        }
    }

    private fun checkForWaiting(task: () -> Unit) {
        val now = System.currentTimeMillis()

        if (now - mLastConnectionTime < RECONNECTION_DELAY_MS) {
            Utils.showWaitingForConnectionDialog(requireActivity(), task) {
                onCancel()
            }
        } else
            task()
        mLastConnectionTime = now
    }

    private fun setLoadingLayout(isLoadingLayout: Boolean) {
        val constraint = if (isLoadingLayout) {
            mNormalConstraintSet.clone(root)
            mLoadingConstraintSet
        } else mNormalConstraintSet

        constraint.applyTo(root)

        ChangeBounds().run {
            interpolator = OvershootInterpolator()
            TransitionManager.beginDelayedTransition(root, this)
        }
    }

    fun updateInfo(string: String) {
        infoTv.text = string
    }

}