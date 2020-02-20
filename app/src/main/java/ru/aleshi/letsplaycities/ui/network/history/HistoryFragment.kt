package ru.aleshi.letsplaycities.ui.network.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_friends.placeholder
import kotlinx.android.synthetic.main.fragment_friends.recyclerView
import kotlinx.android.synthetic.main.fragment_friends.view.*
import kotlinx.android.synthetic.main.fragment_history.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.OnRemovableItemClickListener
import ru.aleshi.letsplaycities.ui.ViewModelFactory
import ru.aleshi.letsplaycities.ui.network.BasicNetworkFetchFragment
import ru.quandastudio.lpsclient.core.LpsApi
import ru.quandastudio.lpsclient.core.LpsRepository
import ru.quandastudio.lpsclient.model.HistoryInfo
import javax.inject.Inject

class HistoryFragment : BasicNetworkFetchFragment<HistoryInfo>(),
    OnRemovableItemClickListener<HistoryInfo> {

    private lateinit var mAdapter: HistoryListAdapter

    @Inject
    lateinit var mPicasso: Picasso

    override fun onCreate(sharedViewModelFactory: ViewModelFactory) = Unit

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

    override fun onStartRequest() = LpsRepository::getHistoryList

    override fun onRequestView() =
        ViewDataHolder(mAdapter, placeholder, recyclerView, loadingProgress, R.string.no_history)

}