package ru.aleshi.letsplaycities.billing

/**
 * Used for callback to dispatch purchase event
 */
interface PurchaseListener {

    /**
     * Called when received purchase event from pay service
     * @param productId purchased product id
     * @param purchaseToken token for validation
     * @param signature signature ofr validation
     */
    fun onPurchased(productId: String, purchaseToken: String, signature: String)

}