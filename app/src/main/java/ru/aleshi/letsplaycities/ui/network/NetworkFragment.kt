package ru.aleshi.letsplaycities.ui.network

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_waiting.view.*
import kotlinx.android.synthetic.main.fragment_network.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.network.NetworkContract
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.game.GameSessionViewModel
import ru.aleshi.letsplaycities.ui.network.friends.FriendsViewModel
import ru.aleshi.letsplaycities.ui.profile.ProfileViewModel
import ru.aleshi.letsplaycities.utils.Event
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.FriendModeResult
import javax.inject.Inject

class NetworkFragment : Fragment(R.layout.fragment_network), NetworkContract.View {

    @Inject
    lateinit var mNetworkPresenter: NetworkContract.Presenter
    @Inject
    lateinit var prefs: GamePreferences
    @Inject
    lateinit var authDataFactory: GameAuthDataFactory

    private val viewModelProvider: ViewModelProvider by lazy {
        ViewModelProvider(requireParentFragment())
    }

    private val args: NetworkFragmentArgs by navArgs()

    private val friendsViewModel: FriendsViewModel
        get() = viewModelProvider[FriendsViewModel::class.java]

    private var gameSound: MediaPlayer? = null
    private var lastConnectionTime: Long = 0
    private val reconnectionDelay = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        friendsViewModel.friendsInfo.observe(this@NetworkFragment) { event: Event<FriendInfo> ->
            event.getContentIfNotHandled()?.let { friendInfo ->
                mNetworkPresenter.onConnect(friendInfo)
            }
        }

        if (prefs.isSoundEnabled()) {
            gameSound = MediaPlayer.create(activity, R.raw.begin)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).setToolbarVisibility(true)

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
            FriendModeResult.NOT_FRIEND -> R.string.not_friend
            FriendModeResult.NO_USER -> R.string.no_user
        }
        Toast.makeText(requireContext(), getString(msgId, login ?: ""), Toast.LENGTH_LONG).show()
    }

    override fun onStartGame(session: GameSession) {
        viewModelProvider[GameSessionViewModel::class.java].setGameSession(session)
        gameSound?.start()
        findNavController().navigate(R.id.start_game_fragment)
    }

    override fun handleError(throwable: Throwable) =
        NetworkUtils.showErrorSnackbar(throwable, this@NetworkFragment)

    override fun showMessage(msgResId: Int) =
        Snackbar.make(requireView(), msgResId, Snackbar.LENGTH_LONG).show()

    override fun onCancel() = setLoadingLayout(false)

    override fun updatePictureHash(userId: Int, picHash: String?) =
        viewModelProvider[ProfileViewModel::class.java].updatePictureHash(userId, picHash)

    override fun setupLayout(isLoggedIn: Boolean, isLocal: Boolean) {
        btnConnect.isVisible = isLoggedIn
        group_social.isVisible = isLoggedIn && !isLocal
    }

    override fun onStop() {
        super.onStop()
        mNetworkPresenter.onDispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameSound?.release()
    }

    override fun onResume() {
        super.onResume()
        val networkViewModel = ViewModelProvider(this)[NetworkViewModel::class.java]
        if (networkViewModel.argsHandled.value != true && "fm_game" == args.action) {
            val cred = authDataFactory.loadCredentials()
            if (cred.isValid()) {
                if (args.targetId != cred.userId) {
                    Toast.makeText(
                        requireContext(),
                        R.string.friend_game_diff_account,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    setLoadingLayout(true)
                    mNetworkPresenter.onConnectToFriendGame(args.oppId)
                    networkViewModel.argsHandled.value = true
                }
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

        lastConnectionTime = System.currentTimeMillis().also { now ->
            if (now - lastConnectionTime < reconnectionDelay * 1000) {
                showWaitingForConnectionDialog(task, mNetworkPresenter::onCancel)
            } else
                task()
        }
    }

    @SuppressLint("InflateParams")
    private fun showWaitingForConnectionDialog(task: () -> Unit, cancelCallback: () -> Unit) {
        var active = true
        val view = layoutInflater.inflate(R.layout.dialog_waiting, null)
        val dialog = with(AlertDialog.Builder(requireActivity())) {
            setCancelable(true)
            setView(view)
            create()
        }

        lifecycleScope.launch {
            for (i in reconnectionDelay downTo 1) {
                if (active) {
                    view.con_waiting_tv.text = getString(R.string.waiting_for_connection, i)
                    delay(1000)
                } else break
            }
            dialog.dismiss()
            if (active) task() else cancelCallback()
        }
        dialog.setOnCancelListener {
            active = false
            cancelCallback()
        }
        dialog.show()
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