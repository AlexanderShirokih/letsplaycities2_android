package ru.aleshi.letsplaycities.ui.network

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_friends.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.BasicListAdapter
import ru.aleshi.letsplaycities.ui.FetchState
import ru.aleshi.letsplaycities.ui.ViewModelFactory
import ru.quandastudio.lpsclient.core.LpsApi
import javax.inject.Inject

// TODO: Test without network
// TODO: History fragment: test when no logged in
abstract class BasicNetworkFetchFragment<FetchDataType> : Fragment() {

    @Inject
    protected lateinit var viewModelFactory: ViewModelFactory

    private lateinit var networkFetchViewModel: NetworkFetchViewModel

    private var views: ViewDataHolder<FetchDataType>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        onCreate(viewModelFactory)
        networkFetchViewModel =
            ViewModelProvider(this, viewModelFactory)[NetworkFetchViewModel::class.java]
        networkFetchViewModel.state.observe(this@BasicNetworkFetchFragment) { state ->
            @Suppress("UNCHECKED_CAST")
            when (state) {
                FetchState.LoadingState -> onLoading()
                is FetchState.DataState<*> -> onData(state.data as List<FetchDataType>)
                is FetchState.ErrorState -> onError(state.error)
                FetchState.FinishState -> onData(emptyList())
            }
        }
    }

    abstract fun onCreate(sharedViewModelFactory: ViewModelFactory)

    class ViewDataHolder<FetchDataType>(
        val adapter: BasicListAdapter<FetchDataType, *>,
        val placeholder: TextView,
        val recyclerView: View,
        val loadingProgress: View,
        @StringRes val emptyViewPlaceholder: Int
    )

    protected fun withApi(action: (api: LpsApi) -> Disposable) {
        networkFetchViewModel.withApi(action)
    }

    abstract fun onRequestView(): ViewDataHolder<FetchDataType>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        views = onRequestView()
        networkFetchViewModel.fetchData(onStartRequest())
    }

    abstract fun onStartRequest(): (api: LpsApi) -> Maybe<List<FetchDataType>>

    protected open fun onLoading() {
        setListVisibility(visible = false, showLoading = true)
        placeholder.setText(R.string.loading_data)
    }

    protected open fun onData(data: List<FetchDataType>) {
        setListVisibility(data.isNotEmpty())
        views?.apply {
            adapter.updateItems(data)
            placeholder.setText(emptyViewPlaceholder)
        }
    }

    protected open fun onError(error: Throwable) {
        setListVisibility(visible = false)
        placeholder.setText(R.string.error_loading_data)
        Toast.makeText(requireContext(), error.message ?: error.toString(), Toast.LENGTH_LONG)
            .show()
    }

    protected fun setListVisibility(visible: Boolean, showLoading: Boolean = false) {
        views?.apply {
            recyclerView.isVisible = visible
            placeholder.isVisible = !visible
            loadingProgress.isVisible = !visible && showLoading
        }
    }
}