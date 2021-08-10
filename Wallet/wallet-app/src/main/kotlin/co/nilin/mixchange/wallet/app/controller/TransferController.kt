package co.nilin.mixchange.wallet.app.controller

import co.nilin.mixchange.wallet.core.inout.TransferCommand
import co.nilin.mixchange.wallet.core.inout.TransferResult
import co.nilin.mixchange.wallet.core.model.Amount
import co.nilin.mixchange.wallet.core.service.TransferService
import co.nilin.mixchange.wallet.core.spi.WalletManager
import co.nilin.mixchange.wallet.core.spi.WalletOwnerManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalArgumentException
import java.math.BigDecimal

@RestController
class TransferController(
    val transferService: TransferService, val walletManager: WalletManager, val walletOwnerManager: WalletOwnerManager
) {
    @PostMapping("/transfer/{amount}_{symbol}/from/{senderUuid}_{senderWalletType}/to/{receiverUuid}_{receiverWalletType}")
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
    suspend fun transfer(
        @PathVariable("symbol") symbol: String,
        @PathVariable("senderWalletType") senderWalletType: String,
        @PathVariable("senderUuid") senderUuid: String,
        @PathVariable("receiverWalletType") receiverWalletType: String,
        @PathVariable("receiverUuid") receiverUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @PathVariable("description") description: String?,
        @PathVariable("transferRef") transferRef: String?
    ): TransferResult {
        val sourceOwner = walletOwnerManager.findWalletOwner(senderUuid) ?: throw IllegalArgumentException()
        val sourceWallet =
            walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, senderWalletType, Symbol(symbol))
                ?: throw IllegalArgumentException()
        val receiverOwner = walletOwnerManager.findWalletOwner(receiverUuid) ?: walletOwnerManager.createWalletOwner(senderUuid, "noset", "")
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            receiverOwner, receiverWalletType, Symbol(symbol)
        ) ?: walletManager.createWallet(receiverOwner, Amount(Symbol(symbol), BigDecimal.ZERO), Symbol(symbol), receiverWalletType)
        return transferService.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency(), amount),
                description, transferRef
            )
        )
    }
}