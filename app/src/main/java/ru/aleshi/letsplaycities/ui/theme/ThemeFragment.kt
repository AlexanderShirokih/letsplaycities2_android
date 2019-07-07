package ru.aleshi.letsplaycities.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_blacklist.*
import ru.aleshi.letsplaycities.BadTokenException
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.SignatureChecker
import ru.aleshi.letsplaycities.base.ThemeManager
import ru.aleshi.letsplaycities.base.ThemeManager.test2
import ru.aleshi.letsplaycities.billing.InAppPurchaseManager
import ru.aleshi.letsplaycities.billing.PurchaseListener

class ThemeFragment : Fragment(), ThemeItemClickListener, PurchaseListener {

    private lateinit var mThemeListAdapter: ThemeListAdapter
    private lateinit var mApplication: LPSApplication
    private lateinit var mInAppPurchaseManager: InAppPurchaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApplication = requireContext().applicationContext as LPSApplication
        mInAppPurchaseManager = InAppPurchaseManager(requireActivity(), this)
        mInAppPurchaseManager.startConnection()

        if (test2()) {
            try {
                throw BadTokenException()
            } catch (e: BadTokenException) {
                e.printStackTrace()
                android.os.Process.killProcess(android.os.Process.myPid())
            }

        }

        ThemeManager.checkAvailable(mApplication)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mThemeListAdapter = ThemeListAdapter(getThemesList(), this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
            adapter = mThemeListAdapter
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
        if (SignatureChecker.check(requireContext()) == "Y") {
            ThemeManager.putTheme(mApplication, productId, purchaseToken, signature)
            requireActivity().recreate()
        }
    }

    override fun onShowPreview(theme: ThemeListAdapter.NamedTheme, position: Int) {
        findNavController().navigate(
            ThemeFragmentDirections.showPreviewImageDialog(
                "scr${position + 1}.png",
                theme.name
            )
        )
    }

    override fun onSelectTheme(namedTheme: ThemeListAdapter.NamedTheme) {
        if (namedTheme.theme.isFreeOrAvailable()) {
            ThemeManager.switchTheme(namedTheme.theme, mApplication)
            requireActivity().recreate()
        } else
            onUnlock(namedTheme)
    }

    private fun getThemesList(): Array<ThemeListAdapter.NamedTheme> {
        val themeNames = resources.getStringArray(R.array.themes)
        val themes = ThemeManager.themes
        var index = 0
        return Array(themes.size) {
            ThemeListAdapter.NamedTheme(themeNames[index], themes[index++])
        }
    }
}