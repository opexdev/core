package co.nilin.opex.referral.core.spi

interface CheckoutHandler {
    suspend fun checkoutById(id: Long)
}
