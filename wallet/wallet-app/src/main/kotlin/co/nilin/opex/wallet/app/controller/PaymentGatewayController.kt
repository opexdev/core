package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.PaymentCurrency
import co.nilin.opex.wallet.app.dto.PaymentDepositRequest
import co.nilin.opex.wallet.app.dto.PaymentDepositResponse
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.TransferManager
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/payment")
class PaymentGatewayController(
    val transferManager: TransferManager,
    val currencyService: CurrencyServiceManager,
    val walletManager: WalletManager,
    val walletOwnerManager: WalletOwnerManager
) {

    @PostMapping("/internal/deposit")
    suspend fun paymentDeposit(@RequestBody request: PaymentDepositRequest): PaymentDepositResponse {
        val receiverWalletType = WalletType.MAIN
        val convertedAmount = when (request.currency) {
            PaymentCurrency.RIALS -> (request.amount / BigDecimal.valueOf(10)).toLong()
            PaymentCurrency.TOMAN -> request.amount.toLong()
        }

        val currency =
            currencyService.fetchCurrency(FetchCurrency(symbol = "IRT")) ?: throw OpexError.CurrencyNotFound.exception()
        val sourceOwner = walletOwnerManager.findWalletOwner(walletOwnerManager.systemUuid)
            ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, WalletType.MAIN, currency)
            ?: walletManager.createWallet(sourceOwner, Amount(currency, BigDecimal.ZERO), currency, WalletType.MAIN)

        val receiverOwner = walletOwnerManager.findWalletOwner(request.userId)
            ?: walletOwnerManager.createWalletOwner(request.userId, "not set", "")

        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            receiverOwner,
            receiverWalletType,
            currency
        ) ?: walletManager.createWallet(
            receiverOwner,
            Amount(currency, BigDecimal.ZERO),
            currency,
            receiverWalletType
        )

        transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency, convertedAmount.toBigDecimal()),
                request.description,
                request.reference,
                TransferCategory.DEPOSIT
            )
        )

        return PaymentDepositResponse(true)
    }

}