package ru.aleshi.letsplaycities.ui.network

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_change_mode.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.databinding.DialogChangeModeBinding
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication


class ChangeModeDialog : DialogFragment() {

    private lateinit var mBinding: DialogChangeModeBinding
    private lateinit var mDescriptions: Array<String>
    val description: ObservableField<String> = ObservableField()

    companion object {
        private const val RADIO_BTN_ID_BASE = 2500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDescriptions = resources.getStringArray(R.array.dcm_descriptions)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val prefs = lpsApplication.gamePreferences
        val currentScoringType = prefs.getCurrentScoringType()

        mBinding =
            DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.dialog_change_mode, null, false)
        description.set(mDescriptions[currentScoringType])

        resources.getStringArray(R.array.scoring).forEachIndexed { index, item ->
            mBinding.root.radioGroup.addView(RadioButton(requireContext()).apply {
                isChecked = index == currentScoringType
                id = index + RADIO_BTN_ID_BASE
                text = item
                setTextColor(resources.getColor(android.R.color.black))
            })
        }

        return AlertDialog.Builder(requireActivity())
            .setView(mBinding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_change_mode_accept) { _, _ ->
                val chId = mBinding.root.radioGroup.checkedRadioButtonId - RADIO_BTN_ID_BASE
                if (chId >= 0) {
                    prefs.setCurrentScoringType(chId)
                }
            }
            .setNegativeButton(R.string.dialog_change_mode_no) { _, _ ->
            }
            .create()
    }

    fun onModeChanged(radioGroup: RadioGroup, id: Int) {
        if (id >= 0) {
            description.set(mDescriptions[id - RADIO_BTN_ID_BASE])
        }
    }
}