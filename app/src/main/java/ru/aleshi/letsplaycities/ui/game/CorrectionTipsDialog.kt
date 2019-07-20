package ru.aleshi.letsplaycities.ui.game

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_correction_tips.view.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.databinding.DialogCorrectionTipsBinding
import ru.aleshi.letsplaycities.utils.Utils

class CorrectionTipsDialog : DialogFragment() {

    val isSearching: ObservableBoolean = ObservableBoolean(true)

    private val disposable: CompositeDisposable = CompositeDisposable()
    private lateinit var gameSessionViewModel: GameSessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameSessionViewModel = ViewModelProviders.of(requireActivity())[GameSessionViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        return AlertDialog.Builder(activity)
            .setTitle(R.string.searching_words)
            .setView(DialogCorrectionTipsBinding.inflate(LayoutInflater.from(activity)).apply {
                dialog = this@CorrectionTipsDialog
            }.root.apply {
                val layoutManager = LinearLayoutManager(activity)
                recyclerView.layoutManager = layoutManager
                recyclerView.addItemDecoration(DividerItemDecoration(activity, layoutManager.orientation))
                loadItems {
                    recyclerView.adapter = it
                }
            })
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
    }

    private fun loadItems(callback: (adapter: CorrectionTipsAdapter) -> Unit) {
        val args = CorrectionTipsDialogArgs.fromBundle(requireArguments())
        val dictionary = gameSessionViewModel.gameSession.value!!.dictionary
        disposable.add(
            Maybe.just(dictionary)
                .subscribeOn(Schedulers.computation())
                .doOnSuccess { isSearching.set(true) }
                .map { it.getCorrectionVariants(args.word) }
                .doOnSuccess { isSearching.set(false) }
                .filter { it.isNotEmpty() }
                .map {
                    CorrectionTipsAdapter(it) { item -> dispatchResult(item, null) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ callback(it) }, ::error, { dispatchResult(null, args.errorMsg) })
        )
    }

    private fun dispatchResult(result: String?, error: String?) {
        gameSessionViewModel.correctedWord.postValue(result to error)
        findNavController().navigateUp()
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }


    class CorrectionTipsAdapter(val list: List<String>, val onClick: (item: String) -> Unit) :
        RecyclerView.Adapter<CorrectionItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CorrectionItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            val holder = CorrectionItemViewHolder(view)
            view.setOnClickListener {
                if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                    onClick(list[holder.adapterPosition])
                }
            }
            return holder
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: CorrectionItemViewHolder, position: Int) {
            holder.bind(list[position])
        }

    }

    class CorrectionItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: String) {
            itemView.findViewById<TextView>(android.R.id.text1).text = Utils.firstToUpper(item)
        }
    }
}