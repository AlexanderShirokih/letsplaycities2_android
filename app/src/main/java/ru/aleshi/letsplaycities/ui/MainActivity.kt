package ru.aleshi.letsplaycities.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.ThemeManager
import ru.aleshi.letsplaycities.utils.DictionaryUpdater


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, findNavController(R.id.main_nav_fragment))
        checkForFirebaseNotifications()
    }

    override fun onResume() {
        super.onResume()
        DictionaryUpdater.checkForUpdates(this)
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