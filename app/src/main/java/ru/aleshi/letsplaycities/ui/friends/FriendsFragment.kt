package ru.aleshi.letsplaycities.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.network.BasicNetworkFetchFragment
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.model.FriendInfo

class FriendsFragment : BasicNetworkFetchFragment<ArrayList<FriendInfo>>(),
    OnRemovableItemClickListener<FriendInfo> {
    companion object {
        private const val REQUEST_CODE_SELECT_ITEM = 1
        private const val REQUEST_CODE_REMOVE_ITEM = 2
    }

    private lateinit var mAdapter: FriendsListAdapter
    private lateinit var mSelectedFriendsInfo: FriendInfo

    override fun onCreate(viewModelProvider: ViewModelProvider) {
        viewModelProvider[ConfirmViewModel::class.java].callback.observe(this,
            Observer<ConfirmViewModel.Request> { request ->
                if (request.result && ::mSelectedFriendsInfo.isInitialized) {
                    when (request.resultCode) {
                        REQUEST_CODE_SELECT_ITEM -> {
                            ViewModelProviders.of(requireActivity())[FriendsViewModel::class.java].friendsInfo.postValue(
                                mSelectedFriendsInfo
                            )
                            findNavController().popBackStack(R.id.networkFragment, false)
                        }
                        REQUEST_CODE_REMOVE_ITEM -> {
                            mNetworkRepository.let { rep ->
                                rep.deleteFriend(mSelectedFriendsInfo.userId)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { mAdapter.removeItem(mSelectedFriendsInfo) }
                            }
                        }
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false).apply {
            recyclerView.run {
                mAdapter = FriendsListAdapter(this@FriendsFragment)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = mAdapter
                setHasFixedSize(true)
            }
        }
    }

    override fun onItemClicked(item: FriendInfo) {
        mSelectedFriendsInfo = item
        val msg = resources.getString(R.string.invite_friend, item.login)
        findNavController().navigate(
            FriendsFragmentDirections.showConfimationDialog(
                REQUEST_CODE_SELECT_ITEM,
                msg,
                null
            )
        )
    }

    override fun onRemoveItemClicked(item: FriendInfo, position: Int) {
        mSelectedFriendsInfo = item
        val msg = resources.getString(R.string.remove_from_friends, item.login)
        findNavController().navigate(
            FriendsFragmentDirections.showConfimationDialog(
                REQUEST_CODE_REMOVE_ITEM,
                msg,
                null
            )
        )
    }

    override fun onStartRequest(networkRepository: NetworkRepository): Single<ArrayList<FriendInfo>> =
        networkRepository.getFriendsList()

    override fun onDataFetched(result: ArrayList<FriendInfo>) {
        mAdapter.updateItems(result)
        if (result.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
        }
    }

}