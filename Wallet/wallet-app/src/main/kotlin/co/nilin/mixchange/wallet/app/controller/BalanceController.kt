package co.nilin.mixchange.wallet.app.controller

import co.nilin.mixchange.wallet.core.spi.WalletManager
import co.nilin.mixchange.wallet.core.spi.WalletOwnerManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal

@RestController
class BalanceController(
    val walletManager: WalletManager, val walletOwnerManager: WalletOwnerManager
) {
    val logger = LoggerFactory.getLogger(BalanceController::class.java)

    data class BalanceResponse(val balance: BigDecimal)

    @GetMapping("/balanceOf/wallet_type/{wallet_type}/currency/{currency}")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"balance\": 990 }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun getBalance(
        principal: Principal,
        @PathVariable("currency") currency: String,
        @PathVariable("wallet_type") walletType: String
    ): BalanceResponse {
        val owner = walletOwnerManager.findWalletOwner(principal.name)
        if (owner != null) {
            val wallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, walletType, Symbol(currency))
            return BalanceResponse(wallet?.balance()?.amount ?: BigDecimal.ZERO)
        }
        return BalanceResponse(BigDecimal.ZERO)
    }
}