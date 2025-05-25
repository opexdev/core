package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.PaymentDepositRequest
import co.nilin.opex.wallet.app.dto.PaymentDepositResponse
import co.nilin.opex.wallet.app.service.TraceDepositService
import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferMethod
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.TransferManager
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping("/payment")
class PaymentGatewayController(
    val transferManager: TransferManager,
    val currencyService: CurrencyServiceManager,
    val walletManager: WalletManager,
    val walletOwnerManager: WalletOwnerManager,
    val traceDepositService: TraceDepositService

) {

    //todo refactor
    @PostMapping("/internal/deposit")
    @Transactional
    suspend fun paymentDeposit(@RequestBody request: PaymentDepositRequest): PaymentDepositResponse {
        val receiverWalletType = WalletType.MAIN
//        val convertedAmount = when (request.currency) {
//            PaymentCurrency.RIALS -> (request.amount / BigDecimal.valueOf(10)).toLong()
//            PaymentCurrency.TOMAN -> request.amount.toLong()
//        }

        val currency =
            currencyService.fetchCurrency(FetchCurrency(symbol = request.currency.name))
                ?: throw OpexError.CurrencyNotFound.exception()
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
                Amount(sourceWallet.currency, request.amount),
                request.description,
                request.reference,
                TransferCategory.DEPOSIT
            )
        )
        var depositCommand = Deposit(
            receiverOwner.uuid,
            UUID.randomUUID().toString(),
            currency.symbol,
            request.amount,
            note = request.description,
            transactionRef = request.reference,
            status = DepositStatus.DONE,
            depositType = DepositType.OFF_CHAIN,
            network = null,
            attachment = null,
            transferMethod = TransferMethod.IPG
        )
        traceDepositService.saveDepositInNewTransaction(depositCommand)

        return PaymentDepositResponse(true)
    }

}