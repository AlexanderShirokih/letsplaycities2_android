package ru.aleshi.letsplaycities.ui.blacklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_blacklist.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.network.BasicNetworkFetchFragment
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.model.BlackListItem

class BlackListFragment : BasicNetworkFetchFragment<List<BlackListItem>>() {

    private lateinit var mAdapter: BlackListAdapter
    private val requestCodeConfirmRemoving = 9352
    private lateinit var confirmViewModel: ConfirmViewModel
    private var callback: (() -> Unit)? = null

    override fun onCreate(viewModelProvider: ViewModelProvider) {
        confirmViewModel = viewModelProvider[ConfirmViewModel::class.java]
        confirmViewModel.callback.observe(this, Observer<ConfirmViewModel.Request> { request ->
            if (request.resultCode == requestCodeConfirmRemoving && request.result) {
                callback?.invoke()
                callback = null
            }
        })
        mAdapter = BlackListAdapter(object : OnItemClickListener {
            override fun onRemove(item: BlackListItem, pos: Int) {
                showConfirmDialog(
                    requireContext().getString(
                        R.string.remove_from_blacklist,
                        item.login
                    )
                ) {
                    mNetworkRepository.removeFromBanList(item.userId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            mAdapter.remove(pos)
                            setListVisibility(mAdapter.itemCount != 0)
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
        return inflater.inflate(R.layout.fragment_blacklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListVisibility(false)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            setHasFixedSize(true)
        }
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onStartRequest(networkRepository: NetworkRepository): Single<List<BlackListItem>> =
        networkRepository.getBlackList()

    override fun onDataFetched(result: List<BlackListItem>) {
        mAdapter.updateItems(result)
        setListVisibility(result.isNotEmpty())
    }

    private fun showConfirmDialog(msg: String, callback: () -> Unit) {
        this.callback = callback
        findNavController().navigate(
            BlackListFragmentDirections.showConfimationDialog(
                requestCodeConfirmRemoving,
                msg,
                null
            )
        )
    }

    private fun setListVisibility(visible: Boolean) {
        recyclerView.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        blacklist_placeholder.visibility = if (visible) View.INVISIBLE else View.VISIBLE
    }
}