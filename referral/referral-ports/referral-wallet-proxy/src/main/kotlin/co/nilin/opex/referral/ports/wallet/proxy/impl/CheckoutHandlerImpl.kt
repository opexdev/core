package co.nilin.opex.referral.ports.wallet.proxy.impl

import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.CommissionPaymentHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

class CheckoutHandlerImpl(
    private val configHandler: ConfigHandler,
    private val paymentHandler: CommissionPaymentHandler
) : CheckoutHandler {
    override suspend fun checkoutById(uuid: String) {
        val min = configHandler.findConfig("default")!!.minPaymentAmount
        val commissions = paymentHandler.findUserCommissionsWhereTotalGreaterAndEqualTo(uuid, min)
        val transferRef = UUID.randomUUID().toString()
        coroutineScope {
            commissions.forEach {
                launch {
                    //TODO Call wallet transfer api if successful
                    paymentHandler.checkout(it.commissionReward.id, transferRef)
                }
            }
        }
    }

    override suspend fun checkoutEveryCandidate() {
        //TODO Fill checkout queue with valid candidates
        //TODO Do the payments
        //TODO Update payment status for each payed commission reward
    }
}
