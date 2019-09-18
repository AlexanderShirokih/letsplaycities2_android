package ru.aleshi.letsplaycities.ui.blacklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_blacklist.*
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.player.GamePlayerDataFactory
import ru.aleshi.letsplaycities.network.AndroidBase64Provider
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.ViewModelFactory
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.BlackListItem
import javax.inject.Inject

class BlackListFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var mNetworkRepository: NetworkRepository
    @Inject
    lateinit var mGamePlayerDataFactory: GamePlayerDataFactory

    private lateinit var mApplication: LPSApplication
    private lateinit var mAdapter: BlackListAdapter
    private val mDisposable: CompositeDisposable = CompositeDisposable()
    private val requestCodeConfirmRemoving = 9352
    private lateinit var confirmViewModel: ConfirmViewModel
    private var callback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mApplication = lpsApplication
        confirmViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory)[ConfirmViewModel::class.java]
        confirmViewModel.callback.observe(this, Observer<ConfirmViewModel.Request> { request ->
            if (request.resultCode == requestCodeConfirmRemoving && request.result) {
                callback?.invoke()
                callback = null
            }
        })
        mAdapter = BlackListAdapter(object : OnItemClickListener {
            override fun onRemove(item: BlackListItem, pos: Int) {
                showConfirmDialog(requireContext().getString(R.string.remove_from_blacklist, item.login)) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_blacklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListVisibility(false)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            setHasFixedSize(true)
        }
        buildBlackList()
    }

    private fun buildBlackList() {
        mGamePlayerDataFactory.load(mApplication.gamePreferences)?.let { userData ->
            mNetworkRepository = NetworkRepository(NetworkClient(AndroidBase64Provider,false, BuildConfig.HOST), NetworkUtils.getToken()).apply {
                mDisposable.addAll(
                    login(userData)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ requestFriendsList() }, ::handleError)
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mDisposable.dispose()
        mNetworkRepository.disconnect()
    }

    private fun requestFriendsList() {
        mDisposable.add(
            mNetworkRepository.getBlackList()
                .doOnEvent { _, _ -> mNetworkRepository.disconnect() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::populateList, ::handleError)
        )
    }

    private fun populateList(list: List<BlackListItem>) {
        mAdapter.updateItems(list)
        setListVisibility(list.isNotEmpty())
    }

    private fun handleError(t: Throwable) {
        NetworkUtils.handleError(t, this)
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