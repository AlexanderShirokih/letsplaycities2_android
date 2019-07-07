package ru.aleshi.letsplaycities.billing

import android.app.Activity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import ru.aleshi.letsplaycities.base.ThemeManager

class InAppPurchaseManager(private val mActivity: Activity, private val mPurchaseListener: PurchaseListener) :
    PurchasesUpdatedListener {

    private var mConnected = false
    private var mSkuDetailsList: MutableList<SkuDetails>? = null
    private val mBillingClient: BillingClient = BillingClient.newBuilder(mActivity).enablePendingPurchases().setListener(this).build()
    private val mBillingClientStateListener: BillingClientStateListener = createListener()

    fun startConnection() {
        mBillingClient.startConnection(mBillingClientStateListener)
    }

    fun launchBillingFlow(sku: String) {
        if (mBillingClient.isReady && mSkuDetailsList != null) {
            mBillingClient.launchBillingFlow(mActivity, buildSkuDetails(sku))
        } else
            startConnection()
    }

    fun destroy() {
        if (mBillingClient.isReady)
            mBillingClient.endConnection()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        purchases?.forEach {
            if (it.purchaseState == Purchase.PurchaseState.PURCHASED) {
                mPurchaseListener.onPurchased(it.sku, it.purchaseToken, it.signature)
            }
        }
    }

    private fun buildSkuDetails(sku: String): BillingFlowParams {
        return BillingFlowParams.newBuilder()
            .setSkuDetails(mSkuDetailsList!!.first { sku == it.sku })
            .build()
    }

    private fun createListener(): BillingClientStateListener {
        return object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    mConnected = true
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                mConnected = false
            }
        }
    }

    private fun querySkuDetails() {
        val skusList = ThemeManager.getSkusList()
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(skusList)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        mBillingClient.querySkuDetailsAsync(skuDetailsParams) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                mSkuDetailsList = skuDetailsList
            }
        }
    }
}