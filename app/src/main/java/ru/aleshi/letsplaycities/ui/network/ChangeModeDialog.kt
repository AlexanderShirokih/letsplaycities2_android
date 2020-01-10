package ru.aleshi.letsplaycities.ui.network

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_change_mode.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.databinding.DialogChangeModeBinding
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication


class ChangeModeDialog : DialogFragment() {

    private lateinit var mBinding: DialogChangeModeBinding
    private lateinit var mDescriptions: Array<String>
    val description: ObservableField<String> = ObservableField()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDescriptions = resources.getStringArray(R.array.dcm_descriptions)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val prefs = lpsApplication.gamePreferences
        val currentScoringType = prefs.getCurrentScoringType()

        mBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.dialog_change_mode,
                null,
                false
            )
        description.set(mDescriptions[currentScoringType])
        mBinding.root.radioGroup.check(R.id.btn_by_points)
        mBinding.root.radioGroup.setOnCheckedChangeListener { _, id ->
            description.set(mDescriptions[getButtonIndexById(id)])
        }

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(mBinding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_change_mode_accept) { _, _ ->
                val chId = getButtonIndexById(mBinding.root.radioGroup.checkedRadioButtonId)
                prefs.setCurrentScoringType(chId)
            }
            .setNegativeButton(R.string.dialog_change_mode_no) { _, _ ->
            }
            .create()
    }

    private fun getButtonIndexById(id: Int): Int {
        return when (id) {
            R.id.btn_by_time -> 1
            R.id.btn_by_surr -> 2
            else -> 0
        }
    }
}