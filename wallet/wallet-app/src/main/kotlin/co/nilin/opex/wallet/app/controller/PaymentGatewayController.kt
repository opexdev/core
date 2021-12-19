package co.nilin.opex.wallet.app.controller

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.PaymentCurrency
import co.nilin.opex.wallet.app.dto.PaymentDepositRequest
import co.nilin.opex.wallet.app.dto.PaymentDepositResponse
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.service.TransferService
import co.nilin.opex.wallet.core.spi.CurrencyService
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
    val transferService: TransferService,
    val currencyService: CurrencyService,
    val walletManager: WalletManager,
    val walletOwnerManager: WalletOwnerManager
) {

    @PostMapping("/internal/deposit")
    suspend fun paymentDeposit(@RequestBody request: PaymentDepositRequest): PaymentDepositResponse {
        val systemUuid = "1"
        val receiverWalletType = "main"
        val convertedAmount = when (request.currency) {
            PaymentCurrency.RIALS -> (request.amount / 10).toLong()
            PaymentCurrency.TOMAN -> request.amount.toLong()
        }

        val currency = currencyService.getCurrency("IRT") ?: throw OpexException(OpexError.CurrencyNotFound)
        val sourceOwner = walletOwnerManager.findWalletOwner(systemUuid)
            ?: throw OpexException(OpexError.WalletOwnerNotFound)
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, "main", currency)
            ?: walletManager.createWallet(sourceOwner, Amount(currency, BigDecimal.ZERO), currency, "main")

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

        val command = transferService.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency(), convertedAmount.toBigDecimal()),
                request.description,
                request.reference,
                emptyMap()
            )
        )

        return PaymentDepositResponse(true)
    }

}