package ru.aleshi.letsplaycities.ui.theme

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_blacklist.*
import ru.aleshi.letsplaycities.BadTokenException
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.ThemeManager
import ru.aleshi.letsplaycities.base.ThemeManager.test2
import ru.aleshi.letsplaycities.billing.InAppPurchaseManager
import ru.aleshi.letsplaycities.billing.PurchaseListener
import ru.aleshi.letsplaycities.social.AchievementService
import ru.aleshi.letsplaycities.utils.Utils.applyTheme
import javax.inject.Inject

/**
 * Fragment that shows list of game themes and provides functions for switching and purchasing themes.
 */
class ThemeFragment : Fragment(R.layout.fragment_theme), ThemeItemClickListener, PurchaseListener {

    @Inject
    lateinit var prefs: GamePreferences

    private lateinit var mThemeListAdapter: ThemeListAdapter
    private lateinit var mInAppPurchaseManager: InAppPurchaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mInAppPurchaseManager = InAppPurchaseManager(requireActivity(), this)
        mInAppPurchaseManager.startConnection()

        //Test for cracking themes
        if (test2()) {
            try {
                throw BadTokenException()
            } catch (e: BadTokenException) {
                e.printStackTrace()
                android.os.Process.killProcess(android.os.Process.myPid())
            }

        }

        ThemeManager.checkAvailable(prefs)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mThemeListAdapter = ThemeListAdapter(getThemesList(), this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mThemeListAdapter
            setHasFixedSize(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mInAppPurchaseManager.destroy()
    }

    override fun onUnlock(theme: ThemeListAdapter.NamedTheme) {
        mInAppPurchaseManager.launchBillingFlow(theme.theme.sku!!)
    }

    override fun onPurchased(productId: String, purchaseToken: String, signature: String) {
        ThemeManager.putTheme(prefs, productId, purchaseToken, signature)
        applyTheme()
    }

    override fun onSelectTheme(namedTheme: ThemeListAdapter.NamedTheme) {
        if (namedTheme.theme.isFreeOrAvailable()) {
            ThemeManager.saveCurrentTheme(prefs, namedTheme.theme)
            applyTheme()
        } else
            onUnlock(namedTheme)
    }

    private fun applyTheme() {
        applyTheme(prefs, requireContext())
        requireActivity().recreate()
    }

    private fun getThemesList(): Array<ThemeListAdapter.NamedTheme> {
        val themeNames = resources.getStringArray(R.array.themes)
        val themes = ThemeManager.themes
        return Array(themes.size) { ThemeListAdapter.NamedTheme(themeNames[it], themes[it]) }
    }

}