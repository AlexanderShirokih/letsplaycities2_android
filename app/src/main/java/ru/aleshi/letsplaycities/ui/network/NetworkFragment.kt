package ru.aleshi.letsplaycities.ui.network

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import ru.aleshi.letsplaycities.ui.game.GameSessionViewModel
import ru.aleshi.letsplaycities.ui.network.friends.FriendsViewModel
import ru.aleshi.letsplaycities.ui.profile.ProfileViewModel
import ru.aleshi.letsplaycities.utils.Utils
import ru.quandastudio.lpsclient.model.FriendModeResult
import javax.inject.Inject

class NetworkFragment : Fragment(R.layout.fragment_network), NetworkContract.View {

    @Inject
    lateinit var mNetworkPresenter: NetworkContract.Presenter

    @Inject
    lateinit var prefs: GamePreferences

    private val viewModelProvider: ViewModelProvider by lazy {
        ViewModelProvider(
            requireParentFragment()
        )
    }

    private val args: NetworkFragmentArgs by navArgs()

    private val mFriendsViewModel: FriendsViewModel
        get() = viewModelProvider[FriendsViewModel::class.java]

    private var mLastConnectionTime: Long = 0
    private val reconnectionDelay = 5
    private var mGameSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mFriendsViewModel.friendsInfo.observe(
            this@NetworkFragment,
            mNetworkPresenter.onFriendsInfo()
        )
        if (prefs.isSoundEnabled()) {
            mGameSound = MediaPlayer.create(activity, R.raw.begin)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as MainActivity
        activity.setToolbarVisibility(true)

        if (prefs.isChangeModeDialogRequested()) {
            findNavController().navigate(R.id.showChangeModeDialog)
        }
        mNetworkPresenter.onAttachView(this, args.isLocal)

        btnFriends.setOnClickListener { findNavController().navigate(R.id.start_friends_fragment) }
        btnHistory.setOnClickListener { findNavController().navigate(R.id.start_history_fragment) }
        btnConnect.setOnClickListener { mNetworkPresenter.onConnect() }
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

    override fun onStartGame(session: GameSession) {
        viewModelProvider[GameSessionViewModel::class.java].setGameSession(
            session
        )
        mGameSound?.start()
        findNavController().navigate(R.id.start_game_fragment)
    }

    override fun handleError(throwable: Throwable) =
        NetworkUtils.showErrorSnackbar(throwable, this@NetworkFragment)

    override fun showMessage(msgResId: Int) =
        Snackbar.make(requireView(), msgResId, Snackbar.LENGTH_LONG).show()

    override fun onCancel() {
        setLoadingLayout(false)
    }

    override fun getProfileViewModel(): ProfileViewModel =
        viewModelProvider[ProfileViewModel::class.java]

    override fun setupLayout(isLoggedIn: Boolean, isLocal: Boolean) {
        btnConnect.isVisible = isLoggedIn
        group_social.isVisible = isLoggedIn && !isLocal
    }

    override fun onStop() {
        super.onStop()
        mNetworkPresenter.onDispose()
    }

    override fun onResume() {
        super.onResume()
        if ("fm_game" == args.action) {
            if (prefs.isLoggedIn()) {
                setLoadingLayout(true)
                mNetworkPresenter.onConnectToFriendGame(args.oppId)
            } else
                Toast.makeText(
                    requireContext(),
                    R.string.sign_to_continue,
                    Toast.LENGTH_LONG
                ).show()
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
        groupLoading.visibility = if (isLoadingLayout) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.fragment).isEnabled = !isLoadingLayout
        setupLayout(false, isLocal = false)
    }

    override fun updateInfo(infoMsgId: Int) {
        infoTv.text = getString(infoMsgId)
    }

}