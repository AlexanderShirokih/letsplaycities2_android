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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_friends.view.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient2
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.network.PlayerData
import ru.aleshi.letsplaycities.network.lpsv3.FriendsInfo
import ru.aleshi.letsplaycities.network.lpsv3.NetworkRepository
import ru.aleshi.letsplaycities.base.AuthData
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.network.NetworkViewModel
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication

class FriendsFragment : Fragment(), FriendsItemListener {
    companion object {
        private const val REQUEST_CODE_SELECT_ITEM = 1
        private const val REQUEST_CODE_REMOVE_ITEM = 2
    }

    private lateinit var mApplication: LPSApplication
    private lateinit var mAdapter: FriendsListAdapter
    private lateinit var mNetworkRepository: NetworkRepository
    private lateinit var mSelectedFriendsInfo: FriendsInfo
    private val mDisposable: CompositeDisposable = CompositeDisposable()

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
                            if (::mNetworkRepository.isInitialized)
                                mNetworkRepository.deleteFriend(mSelectedFriendsInfo.userId)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { mAdapter.removeItem(mSelectedFriendsInfo) }
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
        val userData = PlayerData()
        userData.authData = AuthData.loadFromPreferences(mApplication.gamePreferences)
        userData.userName = "#" + userData.authData!!.userID

        mNetworkRepository = NetworkRepository(NetworkClient2())
        mDisposable.add(
            mNetworkRepository.login(userData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ requestFriendsList() }, ::handleError)
        )
    }

    override fun onStop() {
        super.onStop()
        mDisposable.dispose()
        mNetworkRepository.disconnect()
    }

    private fun requestFriendsList() {
        mDisposable.add(
            mNetworkRepository.getFriendsList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::populateList, ::handleError)
        )
    }

    private fun populateList(list: ArrayList<FriendsInfo>) {
        mAdapter.updateItems(list)
        if (list.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
        }
    }

    private fun handleError(t: Throwable) {
        NetworkUtils.handleError(t, this)
    }

}