package ru.aleshi.letsplaycities.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.ThemeManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, findNavController(R.id.main_nav_fragment))
        MobileAds.initialize(this)
        checkForFirebaseNotifications()
    }

    override fun onSupportNavigateUp() = findNavController(R.id.main_nav_fragment).navigateUp()

    fun setToolbarVisibility(visible: Boolean) {
        if (visible)
            supportActionBar?.show()
        else
            supportActionBar?.hide()
    }

    private fun checkForFirebaseNotifications() {
        //TODO
    }
}
