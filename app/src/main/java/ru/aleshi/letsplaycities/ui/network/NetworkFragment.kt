package ru.aleshi.letsplaycities.ui.network

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
import ru.aleshi.letsplaycities.ui.profile.ProfileViewModel
import ru.aleshi.letsplaycities.utils.Utils
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import ru.quandastudio.lpsclient.model.FriendModeResult
import javax.inject.Inject

class NetworkFragment : Fragment(R.layout.fragment_network), NetworkContract.View {

    @Inject
    lateinit var mNetworkPresenter: NetworkContract.Presenter

    private val mGamePreferences: GamePreferences by lazy { lpsApplication.gamePreferences }
    private val viewModelProvider: ViewModelProvider by lazy { ViewModelProvider(requireActivity()) }

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
        if (mGamePreferences.isSoundEnabled()) {
            mGameSound = MediaPlayer.create(activity, R.raw.begin)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as MainActivity
        activity.setToolbarVisibility(true)

        if (mGamePreferences.isChangeModeDialogRequested()) {
            findNavController().navigate(R.id.showChangeModeDialog)
        }
        mNetworkPresenter.onAttachView(this)

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
        ViewModelProvider(requireActivity())[GameSessionViewModel::class.java].gameSession =
            session
        mGameSound?.start()
        findNavController().navigate(R.id.start_game_fragment)
    }

    override fun handleError(throwable: Throwable) =
        NetworkUtils.showErrorSnackbar(throwable, this@NetworkFragment)

    override fun showMessage(msgResId: Int) =
        Snackbar.make(requireView(), msgResId, Snackbar.LENGTH_LONG).show()

    override fun onCancel() {
        setLoadingLayout(false)
        setupLayout(true)
    }

    override fun notifyAboutUpdates() =
        Snackbar.make(requireView(), R.string.new_version_available, Snackbar.LENGTH_SHORT).show()

    override fun getProfileViewModel(): ProfileViewModel =
        viewModelProvider[ProfileViewModel::class.java]

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
                if (mGamePreferences.isLoggedIn()) {
                    setLoadingLayout(true)
                    mNetworkPresenter.onConnectToFriendGame(nfa.oppId)
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
        groupLoading.visibility = if (isLoadingLayout) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.fragment).isEnabled = !isLoadingLayout
        setupLayout(false)
    }

    override fun updateInfo(infoMsgId: Int) {
        infoTv.text = getString(infoMsgId)
    }

}