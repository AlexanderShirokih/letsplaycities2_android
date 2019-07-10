package ru.aleshi.letsplaycities.ui.network

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.BanManager
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.network.FriendModeResult
import ru.aleshi.letsplaycities.network.NetworkUtils.updateToken
import ru.aleshi.letsplaycities.network.PlayerData
import ru.aleshi.letsplaycities.network.lpsv3.ILogInListener
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient
import ru.aleshi.letsplaycities.social.AuthData
import ru.aleshi.letsplaycities.utils.Utils

class LogInListener(private val mNetworkFragment: NetworkFragment, private val mGamePreferences: GamePreferences) :
    ILogInListener {

    private var mNetworkViewModel: NetworkViewModel =
        ViewModelProviders.of(mNetworkFragment.requireActivity())[NetworkViewModel::class.java]
    private lateinit var mNetworkClient: NetworkClient
    private lateinit var mUserData: PlayerData
    private lateinit var mState: NetworkClient.PlayState

    private var mKicked: Boolean = false
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    var isFromNotification: Boolean = false

    override fun onConnect(nc: NetworkClient, userData: PlayerData, state: NetworkClient.PlayState) {
        mNetworkClient = nc
        mUserData = userData
        mState = state
    }

    override fun onLoggedIn(updatedData: AuthData) {
        updatedData.saveToPreferences(mGamePreferences)
        mainScope.launch {
            if (mState != NetworkClient.PlayState.PLAY) {
                val name = mNetworkViewModel.friendsInfo.value?.name ?: ""
                mNetworkFragment.updateInfo(
                    mNetworkFragment.getString(R.string.connecting_to_friend, name)
                )
            } else
                mNetworkFragment.updateInfo(mNetworkFragment.getString(R.string.waiting_for_opp))
        }
    }

    override fun onPlay(data: PlayerData, youStarter: Boolean) {
        if (mKicked)
            return
        mainScope.launch {
            if (getBanManager().checkInBanList(data.authData!!.userID)) {
                mNetworkClient.kick()
                mainScope.launch {
                    mNetworkFragment.startGame(mUserData, mState, mNetworkViewModel.friendsInfo.value)
                }
                Toast.makeText(mNetworkFragment.requireContext(), R.string.banned_player, Toast.LENGTH_SHORT).show()
                return@launch
            }

            beginGame(data, youStarter)
        }
    }

    override fun onLoginFailed(banReason: String?, connError: String?) {
        mainScope.launch {
            val context = mNetworkFragment.activity!!
            if (banReason != null) {
                Toast.makeText(context, banReason, Toast.LENGTH_LONG).show()
            } else {
                val err = connError ?: "error #04"
                Snackbar.make(
                    mNetworkFragment.requireView(),
                    context.getString(R.string.server_auth_error, err),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    override fun onNewerBuildAvailable() {
        mainScope.launch {
            Snackbar.make(mNetworkFragment.requireView(), R.string.new_version_available, Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    override fun onKicked(isSystem: Boolean, desc: String) {
        mKicked = true
        mainScope.launch {
            if (isSystem)
                Toast.makeText(mNetworkFragment.activity!!, "Вы были отключены системой.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(
                    mNetworkFragment.activity!!,
                    "Подключённый игрок добавил вас в чёрный список",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onRequestFirebaseToken() {
        updateToken()
    }

    override fun onFriendModeRequest(result: FriendModeResult, fLogin: String?, userId: Int) {
        mainScope.launch {
            val activity = mNetworkFragment.requireActivity()
            val login = fLogin ?: mNetworkViewModel.friendsInfo.value?.name ?: ""
            when (result) {
                FriendModeResult.BUSY -> Toast.makeText(
                    activity,
                    activity.getString(R.string.friend_busy, login),
                    Toast.LENGTH_LONG
                ).show()
                FriendModeResult.OFFLINE -> Toast.makeText(
                    activity,
                    activity.getString(R.string.friend_offline, login),
                    Toast.LENGTH_LONG
                ).show()
                FriendModeResult.NOT_FRIEND -> {
                    Toast.makeText(activity, activity.getString(R.string.not_friend, login), Toast.LENGTH_LONG).show()
                    Toast.makeText(activity, activity.getString(R.string.friend_denied, login), Toast.LENGTH_LONG)
                        .show()
                }
                FriendModeResult.DENIED -> Toast.makeText(
                    activity,
                    activity.getString(R.string.friend_denied, login),
                    Toast.LENGTH_LONG
                ).show()
                FriendModeResult.REQUEST -> {
                    handleRequest(login, userId)
                    return@launch
                }
            }
            mNetworkFragment.onCancel()
        }
    }

    private fun handleRequest(name: String, userId: Int) {
        mainScope.launch {
            val msg = mNetworkFragment.getString(R.string.friend_request, name)
            if (isFromNotification)
                mNetworkClient.sendRequestResult(true, userId)
            else
                Utils.makeConfirmDialog(mNetworkFragment.activity!!, msg) { result ->
                    mNetworkClient.sendRequestResult(result, userId)
                }
        }
    }

    private fun beginGame(data: PlayerData, youStarter: Boolean) {
        Log.d("TAG", "Connected to $data")
        //TODO:
    }

    private fun getBanManager(): BanManager {
        return (mNetworkFragment.requireContext().applicationContext as LPSApplication).banManager
    }

}