package co.nilin.opex.referral.ports.wallet.proxy.impl

import co.nilin.opex.referral.core.spi.CheckoutHandler
import co.nilin.opex.referral.core.spi.CommissionPaymentHandler
import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.core.spi.WalletProxy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

class CheckoutHandlerImpl(
    private val configHandler: ConfigHandler,
    private val paymentHandler: CommissionPaymentHandler,
    private val walletProxy: WalletProxy
) : CheckoutHandler {
    override suspend fun checkoutById(uuid: String) {
        val min = configHandler.findConfig("default")!!.minPaymentAmount
        val commissions = paymentHandler.findUserCommissionsWhereTotalGreaterAndEqualTo(uuid, min)
        val transferRef = UUID.randomUUID().toString()
        coroutineScope {
            val totalShare = commissions.sumOf { it.commissionReward.share }
            val paymentAsset = commissions.first().commissionReward.paymentAssetSymbol //TODO Handle asset variance
            if (walletProxy.canFulfil(paymentAsset, "system", "1", totalShare)) {
                walletProxy.transfer(
                    paymentAsset,
                    "system",
                    "1",
                    "main",
                    uuid,
                    totalShare,
                    "",
                    transferRef
                )
            }
            commissions.forEach {
                launch { paymentHandler.checkout(it.commissionReward.id, transferRef) }
            }
        }
    }

    override suspend fun checkoutEveryCandidate(min: BigDecimal) {
        val commissions = paymentHandler.findAllCommissionsWhereTotalGreaterAndEqualTo(min)
            .groupBy { it.commissionReward.rewardedUuid }
        coroutineScope {
            commissions.forEach { (uuid, c) ->
                val transferRef = UUID.randomUUID().toString()
                coroutineScope {
                    val totalShare = c.sumOf { it.commissionReward.share }
                    val paymentAsset = c.first().commissionReward.paymentAssetSymbol //TODO Handle asset variance
                    if (walletProxy.canFulfil(paymentAsset, "system", "1", totalShare)) {
                        walletProxy.transfer(
                            paymentAsset,
                            "system",
                            "1",
                            "main",
                            uuid,
                            totalShare,
                            "",
                            transferRef
                        )
                    }
                    c.forEach { launch { paymentHandler.checkout(it.commissionReward.id, transferRef) } }
                }
            }
        }
    }

    override suspend fun checkoutOlderThan(date: Long) {
        val commissions = paymentHandler.findCommissionsWherePendingDateLessOrEqualThan(date)
            .groupBy { it.commissionReward.rewardedUuid }
        coroutineScope {
            commissions.forEach { (uuid, c) ->
                val transferRef = UUID.randomUUID().toString()
                val totalShare = c.sumOf { it.commissionReward.share }
                val paymentAsset = c.first().commissionReward.paymentAssetSymbol //TODO Handle asset variance
                if (walletProxy.canFulfil(paymentAsset, "system", "1", totalShare)) {
                    walletProxy.transfer(
                        paymentAsset,
                        "system",
                        "1",
                        "main",
                        uuid,
                        totalShare,
                        "",
                        transferRef
                    )
                }
                c.forEach { launch { paymentHandler.checkout(it.commissionReward.id, transferRef) } }
            }
        }
    }
}
