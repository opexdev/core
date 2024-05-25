package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class InquiryController(
        val walletManager: WalletManager, val walletOwnerManager: WalletOwnerManager, val currencyService: CurrencyServiceManager
) {
    private val logger = LoggerFactory.getLogger(InquiryController::class.java)

    data class BooleanResponse(val result: Boolean)

    @GetMapping("{uuid}/wallet_type/{wallet_type}/can_withdraw/{amount}_{currency}")
    @ApiResponse(
            message = "OK",
            code = 200,
            examples = Example(
                    ExampleProperty(
                            value = "{ }",
                            mediaType = "application/json"
                    )
            )
    )
    suspend fun canFulfill(
            @PathVariable("uuid") uuid: String,
            @PathVariable("currency") currency: String,
            @PathVariable("wallet_type") walletType: String,
            @PathVariable("amount") amount: BigDecimal,
            @CurrentSecurityContext securityContext: SecurityContext?

    ): BooleanResponse {
        securityContext?.let {
            if (uuid != securityContext.authentication.name)
                throw OpexException(OpexError.Forbidden)
        }
        logger.info("canFullFill: {} {} {} {}", uuid, currency, walletType, amount)
        val owner = walletOwnerManager.findWalletOwner(uuid)
        if (owner != null) {
            val c = currencyService.fetchCurrencies(FetchCurrency(symbol =  currency))?.currencies?.first() ?: throw OpexError.CurrencyNotFound.exception()
            val wallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, walletType, c)
            if (wallet != null) {
                return BooleanResponse(
                        walletManager.isWithdrawAllowed(wallet, amount)
                                && walletOwnerManager.isWithdrawAllowed(owner, Amount(wallet.currency, amount))
                )
            }
        }
        return BooleanResponse(false)
    }
}