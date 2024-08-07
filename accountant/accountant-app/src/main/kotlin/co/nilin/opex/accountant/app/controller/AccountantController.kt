package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.app.data.PairFeeResponse
import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.PairConfigLoader
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.accountant.ports.walletproxy.data.BooleanResponse
import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent
import co.nilin.opex.matching.engine.core.model.OrderDirection
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class AccountantController(
    val walletProxy: WalletProxy,
    val financialActionLoader: FinancialActionLoader
) {

    private val logger = LoggerFactory.getLogger(AccountantController::class.java)

    @GetMapping("{uuid}/create_order/{amount}_{currency}/allowed")
    suspend fun canCreateOrder(
        @PathVariable("uuid") uuid: String,
        @PathVariable("currency") currency: String,
        @PathVariable("amount") amount: BigDecimal
    ): BooleanResponse {
        val canFulfil = runCatching { walletProxy.canFulfil(currency, "main", uuid, amount) }
            .onFailure { logger.error(it.message) }
            .getOrElse { false }
        return if (canFulfil) {
            val unprocessed =
                financialActionLoader.countUnprocessed(uuid, currency, SubmitOrderEvent::class.simpleName!!)
            BooleanResponse(unprocessed <= 0)
        } else
            BooleanResponse(false)
    }


}