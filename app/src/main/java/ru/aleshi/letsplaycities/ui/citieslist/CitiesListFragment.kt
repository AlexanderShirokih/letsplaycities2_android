package ru.aleshi.letsplaycities.ui.citieslist

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_cities_list.view.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.citieslist.CitiesListViewModel
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.utils.Utils.safeNavigate
import javax.inject.Inject

class CitiesListFragment : Fragment(R.layout.fragment_cities_list) {

    private lateinit var viewModel: CitiesListViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            requireParentFragment(),
            viewModelFactory
        )[CitiesListViewModel::class.java]
        (requireActivity() as MainActivity).setToolbarVisibility(true)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.recyclerView.apply {
            val adapter = CitiesListAdapter()
            this.adapter = adapter

            lifecycleScope.launch {
                viewModel.filteredCities
                    .collectLatest { newItems ->
                        viewModel.setLoading(true)
                        adapter.onUpdatesDelivered(newItems)
                        viewModel.forceStopLoading()
                    }
            }
            lifecycleScope.launch {
                viewModel.loadingChannel.asFlow().collect { state ->
                    view.progressBar.isVisible = state
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cities_list_menu, menu)
        menu.findItem(R.id.actionFilter).setOnMenuItemClickListener {
            safeNavigate(
                findNavController(),
                R.id.citiesListFragment,
                CitiesListFragmentDirections.startCountryFilterDialog()
            )
            true
        }
        (menu.findItem(R.id.actionSearch).actionView as SearchView).apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            queryHint = getString(R.string.cities_list_query_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.cityFilterChannel.offer(newText)
                    return true
                }
            })
        }
    }

}