package ru.aleshi.letsplaycities.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_friends.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.AuthData
import ru.aleshi.letsplaycities.network.HeadlessLoginListener
import ru.aleshi.letsplaycities.network.PlayerData
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo
import ru.aleshi.letsplaycities.network.lpsv3.ILogInListener
import ru.aleshi.letsplaycities.network.lpsv3.IServiceListener
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient.PlayState
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.network.NetworkViewModel
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class FriendsFragment : Fragment(), IServiceListener, FriendsItemListener {
    companion object {
        private const val REQUEST_CODE_SELECT_ITEM = 1
        private const val REQUEST_CODE_REMOVE_ITEM = 2
    }

    private lateinit var mApplication: LPSApplication
    private lateinit var mAdapter: FriendsListAdapter
    private lateinit var mNetworkClient: NetworkClient
    private lateinit var mSelectedFriendsInfo: FriendsInfo
    private var mInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApplication = lpsApplication
        ViewModelProviders.of(requireActivity())[ConfirmViewModel::class.java].callback.observe(this,
            Observer<ConfirmViewModel.Request> { request ->
                if (request.result && ::mSelectedFriendsInfo.isInitialized) {
                    when (request.resultCode) {
                        REQUEST_CODE_SELECT_ITEM -> {
                            ViewModelProviders.of(requireActivity())[NetworkViewModel::class.java].friendsInfo.postValue(
                                mSelectedFriendsInfo
                            )
                            //ConfirmationDialog -> FriendsFragment
                            findNavController().navigateUp()
                            //FriendsFragment -> NetworkFragment
                            findNavController().navigateUp()

                        }
                        REQUEST_CODE_REMOVE_ITEM -> {
                            if (mInitialized)
                                mNetworkClient.sendFriendDeletion(mSelectedFriendsInfo.userId)
                            mAdapter.removeItem(mSelectedFriendsInfo)
                        }
                    }
                }
            })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false).apply {
            recyclerView.run {
                mAdapter = FriendsListAdapter(this@FriendsFragment)
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        (layoutManager as LinearLayoutManager).orientation
                    )
                )
                adapter = mAdapter
            }
        }
    }

    override fun onFriendsItemClicked(friendsInfo: FriendsInfo) {
        mSelectedFriendsInfo = friendsInfo
        val msg = resources.getString(R.string.invite_friend, friendsInfo.name)
        findNavController().navigate(
            FriendsFragmentDirections.showConfimationDialog(
                REQUEST_CODE_SELECT_ITEM,
                msg,
                null
            )
        )
    }

    override fun onRemoveFriendsItem(friendsInfo: FriendsInfo) {
        mSelectedFriendsInfo = friendsInfo
        val msg = resources.getString(R.string.invite_friend, friendsInfo.name)
        findNavController().navigate(
            FriendsFragmentDirections.showConfimationDialog(
                REQUEST_CODE_REMOVE_ITEM,
                msg,
                null
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logIn(object : HeadlessLoginListener() {
            override fun onSuccess(ad: AuthData) {
                requestFriendsList()
                mInitialized = true
            }

            override fun onError(msg: String) {
                CoroutineScope(Dispatchers.Main)
                    .launch {
                        Snackbar.make(requireView(), R.string.service_upd_err, Snackbar.LENGTH_LONG).show()
                    }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        mNetworkClient.disconnect()
    }

    private fun logIn(loginListener: ILogInListener) {
        val userData = PlayerData()
        userData.authData = AuthData.load(mApplication.gamePreferences, null)
        userData.userName = "#" + userData.authData!!.userID

        mNetworkClient = NetworkClient.createNetworkClient(ToasterErrorListener(this))
        mNetworkClient.serviceListener = this
        mNetworkClient.connect(loginListener, userData, PlayState.SERVICE, 0)
    }

    private fun requestFriendsList() {
        mNetworkClient.sendGetFriendsMsg()
    }

    override fun onFriendsList(list: ArrayList<FriendsInfo>) {
        requireActivity().runOnUiThread {
            mAdapter.updateItems(list)
            if (list.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                placeholder.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                placeholder.visibility = View.VISIBLE
            }
        }
    }
}