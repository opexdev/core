package co.nilin.opex.wallet.app.controller

import co.nilin.opex.port.wallet.postgres.dao.WithdrawRepository
import co.nilin.opex.port.wallet.postgres.model.WithdrawModel
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.service.TransferService
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.math.BigDecimal
import java.security.Principal

@RestController
class WithdrawController(
    val withdrawRepository: WithdrawRepository,
    val transferService: TransferService,
    val walletManager: WalletManager,
    val walletOwnerManager: WalletOwnerManager,
    val currencyService: CurrencyService,
    @Value("\${app.system.uuid}") val systemUuid: String
) {

    @GetMapping("/admin/withdraw")
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
    suspend fun searchWithdraws(
        @RequestParam("uuid", required = false) uuid: String?,
        @RequestParam("transaction_ref", required = false) txRef: String?,
        @RequestParam("dest_transaction_ref", required = false) destTxRef: String?,
        @RequestParam("dest_address", required = false) destAddress: String?,
        @RequestParam("status", required = false) status: List<String>?
    ): List<WithdrawModel> {
        return withdrawRepository
            .findByCriteria(uuid, txRef, destTxRef, destAddress, status?.isEmpty() ?: true, status ?: listOf(""))
            .toList()
    }

    @GetMapping("/withdraw")
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
    suspend fun getMyWithdraws(
        principal: Principal,
        @RequestParam("transaction_ref", required = false) txRef: String?,
        @RequestParam("dest_transaction_ref", required = false) destTxRef: String?,
        @RequestParam("dest_address", required = false) destAddress: String?,
        @RequestParam("status", required = false) status: List<String>?
    ): List<WithdrawModel> {
        return withdrawRepository
            .findByCriteria(
                principal.name,
                txRef,
                destTxRef,
                destAddress,
                status?.isEmpty() ?: true,
                status ?: listOf("")
            )
            .toList()
    }

    @PostMapping("/withdraw/{amount}_{symbol}")
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
    suspend fun requestWithdraw(
        principal: Principal,
        @PathVariable("symbol") symbol: String,
        @PathVariable("amount") amount: BigDecimal,
        @PathVariable("description") description: String?,
        @PathVariable("transferRef") transferRef: String?,
        @RequestParam("fee", required = false) fee: Double?,
        @RequestParam("destCurrency") destCurrency: String?,
        @RequestParam("destAddress") destAddress: String?,
        @RequestParam("destNote", required = false) destNote: String?,
    ): TransferResult {
        val currency = currencyService.getCurrency(symbol)
        val owner = walletOwnerManager.findWalletOwner(principal.name) ?: throw IllegalArgumentException()
        val sourceWallet =
            walletManager.findWalletByOwnerAndCurrencyAndType(owner, "main", currency)
                ?: throw IllegalArgumentException()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            owner, "cashout", currency
        ) ?: walletManager.createWallet(
            owner,
            Amount(currency, BigDecimal.ZERO),
            currency,
            "cashout"
        )
        val allAdditionalData = mutableMapOf<String, String?>()
        allAdditionalData["fee"] = fee?.toString()
        allAdditionalData["destCurrency"] = destCurrency
        allAdditionalData["destAddress"] = destAddress
        allAdditionalData["destNote"] = destNote
        allAdditionalData["description"] = description
        return transferService.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency(), amount),
                description, transferRef, allAdditionalData
            )
        )
    }

    @PostMapping(
        "/admin/withdraw/{id}/reject"
    )
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
    suspend fun rejectWithdraw(
        @PathVariable("id") withdrawId: String,
        @RequestParam("statusReason", required = false) statusReason: String?,
        @RequestParam("destNote", required = false) destNote: String?
    ): TransferResult {
        val withdraw = withdrawRepository.findById(withdrawId)
            .awaitFirstOrElse { throw RuntimeException("No matching withdraw request") }
        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw RuntimeException("Wallet not found")
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            sourceWallet.owner(), "main", sourceWallet.currency()
        ) ?: walletManager.createWallet(
            sourceWallet.owner(),
            Amount(sourceWallet.currency(), BigDecimal.ZERO),
            sourceWallet.currency(),
            "main"
        )
        val allAdditionalData = mutableMapOf<String, String?>()
        allAdditionalData["statusReason"] = statusReason
        allAdditionalData["destNote"] = destNote
        allAdditionalData["transactionId"] = withdraw.transactionId
        allAdditionalData["status"] = "REJECTED"
        return transferService.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency(), withdraw.amount),
                withdraw.description, allAdditionalData["transactionRef"], allAdditionalData
            )
        )
    }

    @PostMapping("/admin/withdraw/{id}/accept")
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
    suspend fun acceptWithdraw(
        @PathVariable("id") withdrawId: String,
        @RequestParam("destTransactionRef", required = false) destTransactionRef: String?,
        @RequestParam("destNote", required = false) destNote: String?
    ): TransferResult {
        val system = walletOwnerManager.findWalletOwner(systemUuid) ?: throw IllegalArgumentException()
        val withdraw = withdrawRepository.findById(withdrawId)
            .awaitFirstOrElse { throw RuntimeException("No matching withdraw request") }
        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw RuntimeException("Wallet not found")
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            system, "main", sourceWallet.currency()
        ) ?: walletManager.createWallet(
            system,
            Amount(sourceWallet.currency(), BigDecimal.ZERO),
            sourceWallet.currency(),
            "main"
        )
        val allAdditionalData = mutableMapOf<String, String?>()
        allAdditionalData["destNote"] = destNote
        allAdditionalData["destTransactionRef"] = destTransactionRef
        allAdditionalData["transactionId"] = withdraw.transactionId
        allAdditionalData["status"] = "DONE"
        return transferService.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency(), withdraw.amount),
                withdraw.description, allAdditionalData["transactionRef"], allAdditionalData
            )
        )
    }
}
