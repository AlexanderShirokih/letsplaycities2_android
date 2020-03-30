package ru.aleshi.letsplaycities.ui.blacklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_blacklist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.network.BasicNetworkFetchFragment
import ru.quandastudio.lpsclient.core.LpsRepository
import ru.quandastudio.lpsclient.model.BlackListItem
import java.io.IOException
import javax.inject.Inject

class BlackListFragment : BasicNetworkFetchFragment<BlackListItem>() {

    private lateinit var mAdapter: BlackListAdapter
    private val requestCodeConfirmRemoving = 9352
    private var callback: (() -> Unit)? = null

    @Inject
    lateinit var mPicasso: Picasso

    override fun onCreate() {
        ViewModelProvider(requireParentFragment())[ConfirmViewModel::class.java].callback.observe(
            this,
            Observer { request ->
                if (request.resultCode == requestCodeConfirmRemoving && request.result) {
                    callback?.invoke()
                    callback = null
                }
            })
        mAdapter = BlackListAdapter(mPicasso, object : OnRemovableItemClickListener<BlackListItem> {
            override fun onRemoveItemClicked(item: BlackListItem, position: Int) {
                showConfirmDialog(
                    requireContext().getString(
                        R.string.remove_from_blacklist,
                        item.login
                    )
                ) {
                    withApi {
                        withContext(Dispatchers.Main) {
                            mAdapter.removeAt(position)
                            setListVisibility(mAdapter.itemCount != 0)
                        }
                        try {
                            it.deleteFromBlacklist(item.userId)
                        } catch (error: IOException) {
                            withContext(Dispatchers.Main) {
                                NetworkUtils.showErrorSnackbar(error, this@BlackListFragment)
                            }
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

    override fun onStartRequest() = LpsRepository::getBlackList

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

    override fun onRequestView() =
        ViewDataHolder(
            mAdapter,
            placeholder,
            recyclerView,
            loadingProgress,
            R.string.empty_black_list
        )
}