package ru.aleshi.letsplaycities.ui.network.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_friends.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.ui.network.BasicNetworkFetchFragment
import ru.quandastudio.lpsclient.NetworkRepository
import ru.quandastudio.lpsclient.model.HistoryInfo
import javax.inject.Inject

class HistoryFragment : BasicNetworkFetchFragment<List<HistoryInfo>>(),
    OnRemovableItemClickListener<HistoryInfo> {

    private lateinit var mAdapter: HistoryListAdapter

    @Inject
    lateinit var mPicasso: Picasso

    override fun onCreate(viewModelProvider: ViewModelProvider) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false).apply {
            recyclerView.run {
                mAdapter = HistoryListAdapter(mPicasso, this@HistoryFragment)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = mAdapter
                setHasFixedSize(true)
            }
        }
    }

    override fun onItemClicked(item: HistoryInfo) {

    }

    override fun onStartRequest(networkRepository: NetworkRepository): Single<List<HistoryInfo>> =
        networkRepository.getHistory()

    override fun onDataFetched(result: List<HistoryInfo>) {
        mAdapter.updateItems(result)
        val res = result.isNotEmpty()
        recyclerView.isVisible = res
        placeholder.isVisible = !res
    }

}