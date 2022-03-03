package co.nilin.opex.referral.ports.wallet.proxy.impl

import co.nilin.opex.referral.core.model.CheckoutRecord
import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.CheckoutRecordHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.core.spi.WalletProxy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class CheckoutHandlerImpl(
    private val configHandler: ConfigHandler,
    private val checkoutRecordHandler: CheckoutRecordHandler,
    private val walletProxy: WalletProxy
) : CheckoutHandler {
    override suspend fun checkoutById(uuid: String) {
        val config = configHandler.findConfig("default")!!
        val min = config.minPaymentAmount
        val commissions = checkoutRecordHandler.findUserCommissionsWhereTotalGreaterAndEqualTo(uuid, min)
        checkout(uuid, commissions)
    }

    override suspend fun checkoutEveryCandidate(min: BigDecimal) {
        val commissions = checkoutRecordHandler.findAllCommissionsWhereTotalGreaterAndEqualTo(min)
            .groupBy { it.commissionReward.rewardedUuid }
        coroutineScope { commissions.forEach { (uuid, c) -> checkout(uuid, c) } }
    }

    override suspend fun checkoutOlderThan(date: Date) {
        val commissions = checkoutRecordHandler.findCommissionsWherePendingDateLessOrEqualThan(date)
            .groupBy { it.commissionReward.rewardedUuid }
        coroutineScope { commissions.forEach { (uuid, c) -> checkout(uuid, c) } }
    }

    private suspend fun checkout(uuid: String, commissions: Iterable<CheckoutRecord>) {
        val m = commissions.groupBy { it.commissionReward.paymentCurrency }
        val transferRef = UUID.randomUUID().toString()
        coroutineScope {
            m.forEach { (paymentCurrency, c) ->
                val totalShare = c.sumOf { it.commissionReward.share }
                if (walletProxy.canFulfil(paymentCurrency, "main", "1", totalShare)) {
                    walletProxy.transfer(
                        paymentCurrency,
                        "main",
                        "1",
                        "main",
                        uuid,
                        totalShare,
                        "",
                        transferRef
                    )
                    c.forEach { launch { checkoutRecordHandler.checkout(it.commissionReward.id, transferRef) } }
                }
            }
        }
    }
}
