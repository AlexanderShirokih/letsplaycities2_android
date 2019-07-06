package ru.aleshi.letsplaycities2.ui.blacklist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_blacklist.*
import ru.aleshi.letsplaycities2.LPSApplication
import ru.aleshi.letsplaycities2.R
import ru.aleshi.letsplaycities2.ui.confirmdialog.ConfirmViewModel


class BlackListFragment : Fragment() {

    private lateinit var confirmViewModel: ConfirmViewModel
    private var callback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confirmViewModel = ViewModelProviders.of(requireActivity())[ConfirmViewModel::class.java]
        confirmViewModel.callback.observe(this, Observer<Boolean> { confirmed ->
            if (confirmed!!) {
                callback?.invoke()
                callback = null
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_blacklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListVisibility(false)
        buildBlackList()
    }

    private fun buildBlackList() {
        val banManager = (requireContext().applicationContext as LPSApplication).banManager
        val bannedPlayers = banManager.getBannedPlayersNameList()
        if (bannedPlayers.isNotEmpty()) {
            setListVisibility(true)
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        (layoutManager as LinearLayoutManager).orientation
                    )
                )
                adapter = BlackListAdapter(bannedPlayers.toMutableList(), object : OnItemClickListener {
                    override fun onRemove(item: String, pos: Int) {
                        showConfirmDialog(requireContext().getString(R.string.remove_from_blacklist, item)) {
                            Log.d("TAG", "remove @item from $pos")
                            banManager.removeFromBanList(pos)
                            (adapter as BlackListAdapter).remove(pos)
                            if (bannedPlayers.isEmpty()) {
                                setListVisibility(false)
                            }
                        }
                    }
                })
            }
        }
    }

    private fun showConfirmDialog(msg: String, callback: () -> Unit) {
        this.callback = callback
        findNavController().navigate(BlackListFragmentDirections.showConfimationDialog(msg))
    }

    private fun setListVisibility(visible: Boolean) {
        recyclerView.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        blacklist_placeholder.visibility = if (visible) View.INVISIBLE else View.VISIBLE
    }
}