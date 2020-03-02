package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.utils.TipsListener

/**
 * Encapsulates ad management logic
 */
class AdManager(private val adView: AdView, private val context: Context) {

    private val rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context)

    /**
     * Call to setup ads
     */
    fun setupAds() {
        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = AdListenerHelper(adView)
        rewardedVideoAd.rewardedVideoAdListener = TipsListener(::loadRewardedVideoAd) {
            /*mGameSession::useHint*/
        }
        if (!rewardedVideoAd.isLoaded)
            loadRewardedVideoAd()
    }

    /**
     * Call to show an ad
     */
    fun showAd() {
        if (rewardedVideoAd.isLoaded) {
            rewardedVideoAd.show()
        } else {
            loadRewardedVideoAd()
            Toast.makeText(context, R.string.internet_unavailable, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadRewardedVideoAd() {
        rewardedVideoAd.loadAd(
            context.getString(R.string.rewarded_ad_id),
            AdRequest.Builder().build()
        )
    }
}