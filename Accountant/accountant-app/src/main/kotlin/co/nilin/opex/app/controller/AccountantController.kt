package co.nilin.opex.app.controller

import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.PairConfigLoader
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.matching.core.eventh.events.SubmitOrderEvent
import co.nilin.opex.matching.core.model.OrderDirection
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class AccountantController(
    val walletProxy: WalletProxy,
    val financialActionLoader: FinancialActionLoader,
    val pairConfigLoader: PairConfigLoader
) {
    data class BooleanResponse(val result: Boolean)

    @GetMapping("{uuid}/create_order/{amount}_{currency}/allowed")
    suspend fun canCreateOrder(
        @PathVariable("uuid") uuid: String,
        @PathVariable("currency") currency: String,
        @PathVariable("amount") amount: BigDecimal
    ): BooleanResponse {
        return BooleanResponse(
            financialActionLoader.countUnprocessed(uuid, currency, SubmitOrderEvent::class.simpleName!!) <= 0
                    && walletProxy.canFulfil(currency, "main", uuid, amount)
        )
    }

    @GetMapping(
        value = ["/config/{pair}/fee/{direction}-{userLevel}", "/config/{pair}/fee/{direction}"]
    )
    suspend fun fetchPairFeeConfig(
        @PathVariable("pair") pair: String,
        @PathVariable("direction") direction: OrderDirection,
        @PathVariable("userLevel") level: String?
    ): PairFeeConfig {
        return pairConfigLoader.load(pair, direction, level ?: "")
    }

}