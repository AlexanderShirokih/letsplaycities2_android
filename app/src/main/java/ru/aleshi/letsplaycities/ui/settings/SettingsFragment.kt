package ru.aleshi.letsplaycities.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.ThemeManager
import ru.aleshi.letsplaycities.ui.MainActivity
import javax.inject.Inject

class SettingsFragment : Fragment() {

    @Inject
    lateinit var prefs: GamePreferences
    private lateinit var adapter: SettingsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        ViewModelProvider(requireActivity())[SettingsViewModel::class.java].selectedItem.observe(
            this,
            Observer {
                prefs.putSettingValue(it.first, it.second)
                adapter.updateItem(it.first, it.second)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                            0 -> nav.navigate(R.id.start_theme_fragment)
                            1 -> showSelectVariantDialog(nav, R.array.diff_names, value, position)
                            2 -> showSelectVariantDialog(nav, R.array.scoring, value, position)
                            3 -> showSelectVariantDialog(nav, R.array.timing, value, position)
                            7 -> nav.navigate(R.id.start_score_fragment)
                            8 -> nav.navigate(R.id.start_blacklist_fragment)
                            9 -> showSelectVariantDialog(nav, R.array.dic_upd, value, position)
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

        SettingsItem.disabledVariantName = onOff[0]

        return ArrayList<Array<String>>(10).apply {
                add(
                    arrayOf(
                        ThemeManager.getCurrentThemeName(
                            prefs,
                            requireContext().resources.getStringArray(R.array.themes)
                        )
                    )
                )

                add(resources.getStringArray(R.array.diff_names))
                add(resources.getStringArray(R.array.scoring))
                add(resources.getStringArray(R.array.timing))
                add(onOff)
                add(onOff)
                add(onOff)
                add(arrayOf(resources.getString(R.string.hint_stats)))
                add(arrayOf(resources.getString(R.string.hint_blacklist)))
                add(resources.getStringArray(R.array.dic_upd))
            }
            .zip(prefs.getSettingsValues()) { value: Array<String>, default: Int -> value to default }
            .zip(resources.getStringArray(R.array.settings_item)) { entry: Pair<Array<String>, Int>, name: String ->
                SettingsItem(
                    entry.second,
                    name,
                    entry.first
                )
            }
    }
}