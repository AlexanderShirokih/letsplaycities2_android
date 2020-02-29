package ru.aleshi.letsplaycities.ui.network.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.network.BasicNetworkFetchFragment
import ru.quandastudio.lpsclient.core.LpsRepository
import ru.quandastudio.lpsclient.model.FriendInfo
import javax.inject.Inject

class FriendsFragment : BasicNetworkFetchFragment<FriendInfo>(),
    OnRemovableItemClickListener<FriendInfo> {
    companion object {
        private const val REQUEST_CODE_SELECT_ITEM = 1
        private const val REQUEST_CODE_REMOVE_ITEM = 2
    }

    @Inject
    lateinit var mPicasso: Picasso
    private lateinit var mAdapter: FriendsListAdapter
    private lateinit var mSelectedFriendsInfo: FriendInfo

    override fun onCreate() {
        ViewModelProvider(this)[ConfirmViewModel::class.java].callback.observe(this,
            Observer<ConfirmViewModel.Request> { request ->
                if (request.result && ::mSelectedFriendsInfo.isInitialized) {
                    when (request.resultCode) {
                        REQUEST_CODE_SELECT_ITEM -> {
                            ViewModelProvider(requireParentFragment())[FriendsViewModel::class.java].friendsInfo.postValue(
                                mSelectedFriendsInfo
                            )
                            findNavController().popBackStack(R.id.networkFragment, false)
                        }
                        REQUEST_CODE_REMOVE_ITEM -> {
                            withApi {
                                it.deleteFriend(mSelectedFriendsInfo.userId)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ mAdapter.removeItem(mSelectedFriendsInfo) }
                                        ,
                                        { error ->
                                            NetworkUtils.showErrorSnackbar(
                                                error,
                                                this@FriendsFragment
                                            )
                                        })
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
                mAdapter = FriendsListAdapter(mPicasso, this@FriendsFragment)
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

    override fun onStartRequest() = LpsRepository::getFriendsList

    override fun onRequestView() =
        ViewDataHolder(mAdapter, placeholder, recyclerView, loadingProgress, R.string.no_friends)

}