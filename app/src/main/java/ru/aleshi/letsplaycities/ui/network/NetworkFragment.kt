package ru.aleshi.letsplaycities.ui.network

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
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
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_network.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.network.NetworkContract
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.social.NativeAccess
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.game.GameSessionViewModel
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.RECONNECTION_DELAY_MS
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import java.io.File


class NetworkFragment : Fragment(), NetworkContract.View {

    private lateinit var mApplication: LPSApplication
    private lateinit var mGamePreferences: GamePreferences
    private lateinit var mNetworkViewModel: NetworkViewModel
    private lateinit var mNetworkPresenter: NetworkContract.Presenter
    private var mLastConnectionTime: Long = 0

    private val mNormalConstraintSet: ConstraintSet = ConstraintSet()
    private val mLoadingConstraintSet: ConstraintSet = ConstraintSet()

    private var mGameSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApplication = lpsApplication
        mGamePreferences = mApplication.gamePreferences
        mNetworkViewModel = ViewModelProviders.of(requireActivity())[NetworkViewModel::class.java]
        mNetworkViewModel.run {
            mNetworkPresenter = networkPresenter
            avatarPath.value = mApplication.gamePreferences.getAvatarPath()
            avatarPath.observe(this@NetworkFragment, Observer {
                if (it == null) {
                    roundedImageView?.setImageResource(R.drawable.ic_player)
                } else {
                    Utils.loadAvatar(File(it).toUri())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { bitmap ->
                            roundedImageView?.setImageBitmap(bitmap)
                        }
                        .subscribe()
                }
            })
            nativeLogin.observe(this@NetworkFragment, Observer { NativeAccess.login(it!!, requireActivity()) })
            friendsInfo.observe(this@NetworkFragment, mNetworkPresenter.onFriendsInfo(getVersionInfo()))
        }
        if (mGamePreferences.isSoundEnabled()) {
            mGameSound = MediaPlayer.create(activity, R.raw.begin)
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
        mNetworkPresenter.onAttachView(this)

        btnVk.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.VK))
        btnOk.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.OK))
        btnFb.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.FB))
        btnGl.setOnClickListener(OnSocialButtonClickedListener(activity, ServiceType.GL))
        btnLogout.setOnClickListener {
            mNetworkPresenter.onLogout()
        }

        btnLoginNoSn.setOnClickListener {
            val extras =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    FragmentNavigatorExtras(roundedImageView to "transition_avatar")
                } else FragmentNavigatorExtras()

            findNavController().navigate(R.id.showLoginDialog, null, null, extras)
        }

        btnFriends.setOnClickListener {
            findNavController().navigate(R.id.start_friends_fragment)
        }

        btnConnect.setOnClickListener {
            mNetworkPresenter.onConnect(getVersionInfo())
        }
        btnCancel.setOnClickListener {
            mNetworkPresenter.onCancel()
        }
    }

    private fun getVersionInfo(): Pair<String, Int> {
        val context = requireContext()
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName to PackageInfoCompat.getLongVersionCode(pInfo).toInt()
    }

    override fun onStartGame(session: GameSession) {
        ViewModelProviders.of(requireActivity())[GameSessionViewModel::class.java].gameSession.value = session
        mGameSound?.start()
        findNavController().navigate(R.id.start_game_fragment)
    }

    override fun handleError(throwable: Throwable) = NetworkUtils.handleError(throwable, this@NetworkFragment)

    override fun showMessage(msgResId: Int) = Snackbar.make(requireView(), msgResId, Snackbar.LENGTH_LONG).show()

    override fun getGamePreferences(): GamePreferences = mGamePreferences

    override fun onCancel() = setLoadingLayout(false)

    override fun notifyAboutUpdates() =
        Snackbar.make(requireView(), R.string.new_version_available, Snackbar.LENGTH_SHORT).show()

    override fun setup() {
        sn_desc.setText(R.string.networkmode_auth_desc)
        group_sn.visibility = View.VISIBLE
        group_connect.visibility = View.GONE

        mNetworkViewModel.avatarPath.value = null
    }

    override fun setupWithSN() {
        group_sn.visibility = View.GONE
        group_connect.visibility = View.VISIBLE
        sn_desc.text = mApplication.gamePreferences.getLogin()
//        checkForRequest()
    }

    override fun onStop() {
        super.onStop()
        mNetworkPresenter.onDispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        SocialNetworkManager.onActivityResult(requireActivity() as MainActivity, requestCode, resultCode, data)
    }

    override fun checkForWaiting(task: () -> Unit) {
        setLoadingLayout(true)

        val now = System.currentTimeMillis()
        if (now - mLastConnectionTime < RECONNECTION_DELAY_MS) {
            Utils.showWaitingForConnectionDialog(requireActivity(), task) {
                mNetworkPresenter.onCancel()
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

    override fun updateInfo(infoMsgId: Int) {
        infoTv.text = getString(infoMsgId)
    }

}