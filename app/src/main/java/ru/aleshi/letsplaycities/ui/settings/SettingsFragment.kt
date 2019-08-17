package ru.aleshi.letsplaycities.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.ThemeManager
import ru.aleshi.letsplaycities.ui.MainActivity


class SettingsFragment : Fragment() {
    private lateinit var prefs: GamePreferences
    private lateinit var adapter: SettingsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = (requireContext().applicationContext as LPSApplication).gamePreferences
        ViewModelProviders.of(requireActivity())[SettingsViewModel::class.java].selectedItem.observe(this, Observer {
            prefs.putSettingValue(it.first, it.second)
            adapter.updateItem(it.first, it.second)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).setToolbarVisibility(true)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this@SettingsFragment.adapter = SettingsListAdapter(createSettingsItems(), object :
                SettingsListAdapter.OnItemClickListener {
                override fun onItemClicked(position: Int, value: SettingsItem) {
                    if (value.canBeEnabled() && !value.hasAdvancedVariants()) {
                        prefs.putSettingValue(position, value.currentValuePosition)
                    } else {
                        val nav = findNavController()
                        when (position) {
                            0 -> nav.navigate(R.id.start_addcity_fragment)
                            1 -> nav.navigate(R.id.start_theme_fragment)
                            2 -> showSelectVariantDialog(nav, R.array.diff_names, value, position)
                            3 -> showSelectVariantDialog(nav, R.array.scoring, value, position)
                            4 -> showSelectVariantDialog(nav, R.array.timing, value, position)
                            8 -> nav.navigate(R.id.start_score_fragment)
                            9 -> nav.navigate(R.id.start_blacklist_fragment)
                            10 -> showSelectVariantDialog(nav, R.array.dic_upd, value, position)
                        }
                    }
                }
            })
            adapter = this@SettingsFragment.adapter
        }
    }

    private fun showSelectVariantDialog(
        nav: NavController,
        items: Int,
        item: SettingsItem,
        position: Int
    ) {
        if (nav.currentDestination?.id == R.id.settingsFragment)
            nav.navigate(
                SettingsFragmentDirections.showSelectVariantDialog(
                    items,
                    item.currentValuePosition,
                    item.name,
                    position
                )
            )
    }

    private fun createSettingsItems(): List<SettingsItem> {
        val onOff = resources.getStringArray(R.array.on_off)
        val names = resources.getStringArray(R.array.settings_item)

        val defaults = prefs.getSettingsValues()
        val values = arrayOfNulls<Array<String>>(11)

        values[0] = arrayOf(resources.getString(R.string.hint_addcity))
        values[1] = arrayOf(ThemeManager.getCurrentThemeName(requireContext()))
        values[2] = resources.getStringArray(R.array.diff_names)
        values[3] = resources.getStringArray(R.array.scoring)
        values[4] = resources.getStringArray(R.array.timing)
        values[5] = onOff
        values[6] = values[5]
        values[7] = values[5]
        values[8] = arrayOf(resources.getString(R.string.hint_stats))
        values[9] = arrayOf(resources.getString(R.string.hint_blacklist))
        values[10] = resources.getStringArray(R.array.dic_upd)

        val items = mutableListOf<SettingsItem>()

        for (index in 0 until names.size) {
            items.add(SettingsItem(defaults[index], names[index], values[index]!!))
        }
        SettingsItem.disabledVariantName = onOff[0]
        return items
    }
}