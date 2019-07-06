package ru.aleshi.letsplaycities2.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.aleshi.letsplaycities2.LPSApplication
import ru.aleshi.letsplaycities2.R
import ru.aleshi.letsplaycities2.base.GamePreferences
import ru.aleshi.letsplaycities2.ui.MainActivity
import ru.aleshi.letsplaycities2.ui.ThemeManager


class SettingsFragment : Fragment() {
    private lateinit var prefs: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = (requireContext().applicationContext as LPSApplication).gamePreferences
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).setToolbarVisibility(true)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
            adapter = SettingsListAdapter(createSettingsItems(), object :
                SettingsListAdapter.OnItemClickListener {
                override fun onItemClicked(position: Int, value: Int) {
                    if (value > -1) {
                        prefs.putSettingValue(position, value)
                    } else {
                        val nav = findNavController()
                        when (position) {
                            0 -> nav.navigate(R.id.start_addcity_fragment)
                            1 -> nav.navigate(R.id.start_theme_fragment)
                            8 -> nav.navigate(R.id.start_score_fragment)
                            9 -> nav.navigate(R.id.start_blacklist_fragment)
                        }
                    }
                }
            })
        }
    }

    private fun createSettingsItems(): List<SettingsItem> {
        val names = resources.getStringArray(R.array.settings_item)

        val defaults = prefs.getSettingsValues()
        val values = arrayOfNulls<Array<String>>(11)

        values[0] = emptyArray()
        values[1] = arrayOf(ThemeManager.getCurrentThemeName(requireContext()))
        values[2] = resources.getStringArray(R.array.diff_names)
        values[3] = resources.getStringArray(R.array.scoring)
        values[4] = resources.getStringArray(R.array.timing)
        values[5] = resources.getStringArray(R.array.on_off)
        values[6] = values[5]
        values[7] = values[5]
        values[8] = emptyArray()
        values[9] = emptyArray()
        values[10] = resources.getStringArray(R.array.dic_upd)

        val items = mutableListOf<SettingsItem>()

        for (index in 0 until names.size) {
            items.add(SettingsItem(defaults[index], names[index], values[index]!!))
        }
        return items
    }
}