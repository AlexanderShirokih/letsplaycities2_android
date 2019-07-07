package ru.aleshi.letsplaycities.billing

interface PurchaseListener {

    fun onPurchased(productId: String, purchaseToken: String, signature: String)
}