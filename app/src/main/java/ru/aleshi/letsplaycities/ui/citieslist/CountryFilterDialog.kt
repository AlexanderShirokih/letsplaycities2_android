package ru.aleshi.letsplaycities.ui.citieslist

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckedTextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_country_filter.view.*
import ru.aleshi.letsplaycities.R
import javax.inject.Inject

class CountryFilterDialog : DialogFragment() {

    private lateinit var viewModel: CountryFilterDialogViewModel

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProvider.Factory


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        val viewModelProvider = ViewModelProvider(this, viewModelProviderFactory)
        viewModel = viewModelProvider[CountryFilterDialogViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val view = activity.layoutInflater.inflate(R.layout.dialog_country_filter, null).apply {
            ctvSelectAll.setOnClickListener { viewModel.switchAll(!(it as CheckedTextView).isChecked) }
            recyclerView.adapter = CountryFilterAdapter(viewModel::setCheckboxState).apply {
                viewModel.checkedAll.observe(this@CountryFilterDialog, ctvSelectAll::setChecked)
                viewModel.countryListWithCheckboxes.observe(this@CountryFilterDialog, this)
            }
        }
        return AlertDialog.Builder(activity)
            .setView(view)
            .setPositiveButton(R.string.apply) { _, _ ->
                viewModel.dispatchSelectedCitiesTo(
                    ViewModelProvider(
                        requireParentFragment(),
                        viewModelProviderFactory
                    )[CitiesListViewModel::class.java].countryListFilterChannel
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
    }

}