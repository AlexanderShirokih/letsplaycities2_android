package ru.aleshi.letsplaycities.utils

import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAdListener

class TipsListener(private val loadRewardedVideoAd: () -> Unit, private val onRewarded: () -> Unit) :
    RewardedVideoAdListener {

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {

    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoCompleted() {
    }

    override fun onRewardedVideoAdClosed() {
        onRewarded()
        loadRewardedVideoAd()
    }

    override fun onRewarded(rewardItem: RewardItem) {
//        onRewarded()
    }

    override fun onRewardedVideoAdLeftApplication() {

    }

    override fun onRewardedVideoAdFailedToLoad(i: Int) {

    }
}