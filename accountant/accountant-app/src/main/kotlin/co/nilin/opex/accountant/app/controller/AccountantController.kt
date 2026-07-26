package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.core.model.WalletType
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.accountant.core.spi.WithdrawLimitManager
import co.nilin.opex.accountant.ports.walletproxy.data.BooleanResponse
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class AccountantController(
    val walletProxy: WalletProxy,
    val financialActionLoader: FinancialActionLoader,
    val withdrawLimitManager: WithdrawLimitManager,

    ) {

    private val logger = LoggerFactory.getLogger(AccountantController::class.java)

    @GetMapping("{uuid}/create_order/{amount}_{currency}/allowed")
    suspend fun canCreateOrder(
        @PathVariable("uuid") uuid: String,
        @PathVariable("currency") currency: String,
        @PathVariable("amount") amount: BigDecimal
    ): BooleanResponse {
        val canFulfil = runCatching { walletProxy.canFulfil(currency, WalletType.MAIN, uuid, amount) }
            .onFailure { logger.error(it.message) }
            .getOrElse { false }
        return if (canFulfil) {
            val unprocessed =
                financialActionLoader.countUnprocessed(uuid, currency, SubmitOrderEvent::class.simpleName!!)
            BooleanResponse(unprocessed <= 0)
        } else
            BooleanResponse(false)
    }

    @GetMapping("{uuid}/{level}/request_withdraw/{amount}_{currency}/allowed")
    suspend fun canRequestWithdraw(
        @PathVariable("uuid") uuid: String,
        @PathVariable("level") userLevel: String,
        @PathVariable("currency") currency: String,
        @PathVariable("amount") amount: BigDecimal
    ): Boolean {
        return withdrawLimitManager.canWithdraw(uuid, userLevel, currency, amount)
    }


}