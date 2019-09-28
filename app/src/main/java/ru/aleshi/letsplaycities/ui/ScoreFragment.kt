package ru.aleshi.letsplaycities.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_score.*
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.scoring.ScoringField
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_ANDROID
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_DIFF_COUNTRY
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_LONG_WORD
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_LOSE
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_NETWORK
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_ONLINE
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_P
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_PLAYER
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_QUICK_TIME
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_SAME_COUNTRY
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_SHORT_WORD
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_TIME
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_WINS
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_BIG_CITIES
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_COMBO
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_FRQ_CITIES
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_HISCORE
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_ONLINE
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_PARTS
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.V_EMPTY_S
import ru.aleshi.letsplaycities.base.scoring.ScoringSet
import ru.aleshi.letsplaycities.utils.StringUtils

class ScoreFragment : Fragment() {

    private val groups = arrayOf(G_PARTS, G_ONLINE, G_HISCORE, G_COMBO, G_FRQ_CITIES, G_BIG_CITIES)
    private val fields = arrayOf(
        F_QUICK_TIME,
        F_SHORT_WORD,
        F_LONG_WORD,
        F_SAME_COUNTRY,
        F_DIFF_COUNTRY,
        F_ANDROID,
        F_PLAYER,
        F_NETWORK,
        F_ONLINE,
        F_TIME,
        F_WINS,
        F_LOSE,
        F_P
    )
    private lateinit var groupNames: Array<String>
    private lateinit var fieldNames: Array<String>
    private lateinit var prefs: GamePreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_score, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = (requireContext().applicationContext as LPSApplication).gamePreferences
        groupNames = resources.getStringArray(R.array.group_names)
        fieldNames = resources.getStringArray(R.array.field_names)

        loadStats()
    }

    private fun loadStats() {
        //Load or build stats
        val allGroups = ScoringGroupsHelper.fromPreferences(prefs)
        buildLayout(stat_root, allGroups)
    }

    private fun buildLayout(root: LinearLayout, set: ScoringSet) {
        for (i in 0 until set.getSize()) {
            val group = set.getGroupAt(i)
            buildMainFieldLayout(root, group.main, i == 0)

            var hasActiveChilds = false
            for (field in group.child)
                if (buildChildFieldLayout(root, field))
                    hasActiveChilds = true

            if (!hasActiveChilds) {
                buildChildFieldLayout(root, ScoringField(""))
            }
        }

        val space = View(requireContext())
        space.minimumWidth = px2dp(1)
        space.minimumHeight = px2dp(25)
        root.addView(space)
    }

    private fun buildMainFieldLayout(root: LinearLayout, main: ScoringField, isTop: Boolean) {
        root.addView(LinearLayout(requireContext()).apply {
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, if (isTop) 0 else px2dp(15), 0, px2dp(2))

            addView(TextView(requireContext()).apply {
                layoutParams =
                    TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                text = translate(main.name)
            })

            if (main.hasValue()) {
                addView(TextView(requireContext()).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    text = main.value()
                })
            }
        })
    }

    private fun buildChildFieldLayout(root: LinearLayout, field: ScoringField): Boolean {
        val ll = LinearLayout(requireContext()).apply {
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            orientation = LinearLayout.HORIZONTAL
            setPadding(px2dp(25), 0, 0, 0)
        }

        if (field.hasValue() && field.value() == V_EMPTY_S)
            return false

        val name = TextView(requireContext()).apply {
            layoutParams =
                TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            text = translate(field.name)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

        }
        ll.addView(name)

        if (field.hasValue()) {
            ll.addView(TextView(requireContext()).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

                if (field.name.startsWith(F_P)) {
                    val v = field.value()
                    val indOf = v.indexOf('=')
                    if (indOf > 0) {
                        text = v.substring(indOf + 1)
                        name.text = StringUtils.toTitleCase(v.substring(0, indOf))
                    } else {
                        text = v.length.toString()
                        name.text = StringUtils.toTitleCase(v)
                    }
                } else
                    text = field.value()
            })
        }

        root.addView(ll)
        return true
    }

    private fun translate(key: String): String {
        for (i in groups.indices)
            if (groups[i] == key)
                return groupNames[i]

        for (i in fields.indices)
            if (fields[i] == key)
                return fieldNames[i]
        return getString(R.string.missing)
    }

    private fun px2dp(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }
}