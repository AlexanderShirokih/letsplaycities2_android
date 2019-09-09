package ru.aleshi.letsplaycities.ui.network

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_network.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.network.NetworkContract
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.friends.FriendsViewModel
import ru.aleshi.letsplaycities.ui.game.GameSessionViewModel
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import ru.quandastudio.lpsclient.model.FriendModeResult
import javax.inject.Inject

class NetworkFragment : Fragment(R.layout.fragment_network), NetworkContract.View {

    @Inject
    lateinit var mNetworkPresenter: NetworkContract.Presenter

    private val mGamePreferences: GamePreferences by lazy { lpsApplication.gamePreferences }
    private val mFriendsViewModel: FriendsViewModel by lazy {
        ViewModelProviders.of(requireActivity())[FriendsViewModel::class.java]
    }
    private var mLastConnectionTime: Long = 0
    private val reconnectionDelay = 5

    private val mNormalConstraintSet: ConstraintSet = ConstraintSet()
    private val mLoadingConstraintSet: ConstraintSet = ConstraintSet()

    private var mGameSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mFriendsViewModel.friendsInfo.observe(
            this@NetworkFragment,
            mNetworkPresenter.onFriendsInfo(getVersionInfo())
        )
        if (mGamePreferences.isSoundEnabled()) {
            mGameSound = MediaPlayer.create(activity, R.raw.begin)
        }
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

        btnFriends.setOnClickListener { findNavController().navigate(R.id.start_friends_fragment) }
        btnConnect.setOnClickListener { mNetworkPresenter.onConnect(getVersionInfo()) }
        btnCancel.setOnClickListener { mNetworkPresenter.onCancel() }
    }

    override fun onFriendModeResult(result: FriendModeResult, login: String?) {
        val msgId = when (result) {
            FriendModeResult.BUSY -> R.string.friend_busy
            FriendModeResult.DENIED -> R.string.friend_denied
            FriendModeResult.OFFLINE -> R.string.friend_offline
            FriendModeResult.NOT_FRIEND -> R.string.not_friend
        }
        Toast.makeText(requireContext(), getString(msgId, login ?: ""), Toast.LENGTH_LONG).show()
    }

    private fun getVersionInfo(): Pair<String, Int> {
        val context = requireContext()
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName to PackageInfoCompat.getLongVersionCode(pInfo).toInt()
    }

    override fun onStartGame(session: GameSession) {
        ViewModelProviders.of(requireActivity())[GameSessionViewModel::class.java].gameSession =
            session
        mGameSound?.start()
        findNavController().navigate(R.id.start_game_fragment)
    }

    override fun handleError(throwable: Throwable) =
        NetworkUtils.handleError(throwable, this@NetworkFragment)

    override fun showMessage(msgResId: Int) =
        Snackbar.make(requireView(), msgResId, Snackbar.LENGTH_LONG).show()

    override fun getGamePreferences(): GamePreferences = mGamePreferences

    override fun onCancel() = setLoadingLayout(false)

    override fun notifyAboutUpdates() =
        Snackbar.make(requireView(), R.string.new_version_available, Snackbar.LENGTH_SHORT).show()

    override fun setupLayout(isLoggedIn: Boolean) {
        group_connect.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        mNetworkPresenter.onDispose()
    }

    override fun onResume() {
        super.onResume()
        arguments?.let { args ->
            val nfa = NetworkFragmentArgs.fromBundle(args)
            if ("fm_game" == nfa.action) {
                if (mGamePreferences.isLoggedFromAnySN()) {
                    mNetworkPresenter.onConnectToFriendGame(getVersionInfo(), nfa.oppId)
                } else
                    Toast.makeText(
                        requireContext(),
                        R.string.sign_to_continue,
                        Toast.LENGTH_LONG
                    ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        SocialNetworkManager.onActivityResult(
            requireActivity() as MainActivity,
            requestCode,
            resultCode,
            data
        )
    }

    override fun checkForWaiting(task: () -> Unit) {
        setLoadingLayout(true)

        val now = System.currentTimeMillis()
        if (now - mLastConnectionTime < reconnectionDelay * 1000) {
            Utils.showWaitingForConnectionDialog(reconnectionDelay, requireActivity(), task) {
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