package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_correction_tips.*
import kotlinx.android.synthetic.main.dialog_correction_tips.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.GameViewModel
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.aleshi.letsplaycities.databinding.DialogCorrectionTipsBinding
import ru.aleshi.letsplaycities.utils.StringUtils

/**
 * Dialog for selecting correction variants and waiting until they were loaded
 */
class CorrectionTipsDialog : DialogFragment() {

    val isSearching: ObservableBoolean = ObservableBoolean(true)

    private lateinit var gameViewModel: GameViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        return MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.searching_words)
            .setView(DialogCorrectionTipsBinding.inflate(LayoutInflater.from(activity)).apply {
                dialog = this@CorrectionTipsDialog
            }.root.apply {
                val layoutManager = LinearLayoutManager(activity)
                recyclerView.layoutManager = layoutManager
                recyclerView.addItemDecoration(
                    DividerItemDecoration(
                        activity,
                        layoutManager.orientation
                    )
                )
            })
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        gameViewModel = ViewModelProvider(requireParentFragment())[GameViewModel::class.java]
        gameViewModel.wordState.observe(this) { state ->
            when (state) {
                //We started with this state, so we don't need do something
                is WordCheckingResult.OriginalNotFound -> Unit
                //We have a corrections, so pass them to adapter
                is WordCheckingResult.Corrections -> recyclerView.adapter =
                    CorrectionTipsAdapter(state.corrections) {
                        //Re-run processing city with corrected word
                        gameViewModel.processCityInput(it)
                    }
                else -> findNavController().navigateUp()
            }
        }
    }

    /**
     * RecyclerView adapter that holds correction variants
     */
    class CorrectionTipsAdapter(val list: List<String>, val onClick: (item: String) -> Unit) :
        RecyclerView.Adapter<CorrectionItemViewHolder>() {

        /**
         * Called when recycler view creates layout
         */
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CorrectionItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            val holder = CorrectionItemViewHolder(view)
            view.setOnClickListener {
                if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                    onClick(list[holder.adapterPosition])
                }
            }
            return holder
        }

        /**
         * Returns item count; it's just list size
         */
        override fun getItemCount(): Int = list.size

        /**
         * Called by recycler view to bind item it's view holder
         */
        override fun onBindViewHolder(holder: CorrectionItemViewHolder, position: Int) {
            holder.bind(list[position])
        }

    }

    /**
     * ViewHolder that binds list items to data
     */
    class CorrectionItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /**
         * Binds [item] to list item text
         * @param item item to be attached to the list item
         */
        fun bind(item: String) {
            itemView.findViewById<TextView>(android.R.id.text1).text = StringUtils.toTitleCase(item)
        }
    }
}