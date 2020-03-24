package ru.aleshi.letsplaycities.ui.citieslist

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.utils.Utils.safeNavigate

class CitiesListFragment : Fragment(R.layout.fragment_cities_list) {

    private lateinit var viewModel: CitiesListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment())[CitiesListViewModel::class.java]
        (requireActivity() as MainActivity).setToolbarVisibility(true)
        setHasOptionsMenu(true)
        viewModel.citiesFilter.observe(this) { disabledCountries: List<Short> ->
            Log.d("TAG", "Selected items: ${disabledCountries.size}")
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

                override fun onQueryTextChange(newText: String?): Boolean {
                    //TODO
                    return false
                }
            })
        }
    }

}