package ru.aleshi.letsplaycities.ui.game

import android.view.View
import com.google.android.gms.ads.AdListener

class AdListenerHelper(private val adView: View?) : AdListener() {

    override fun onAdClosed() {
        adView?.visibility = View.GONE
    }

    override fun onAdLoaded() {
        adView?.visibility = View.VISIBLE
    }

    override fun onAdFailedToLoad(error: Int) {
        adView?.visibility = View.GONE
    }
}