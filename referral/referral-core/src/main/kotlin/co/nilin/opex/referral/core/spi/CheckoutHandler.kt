package co.nilin.opex.referral.core.spi

interface CheckoutHandler {
    suspend fun checkoutById(uuid: String)
    suspend fun checkoutEveryCandidate()
}
