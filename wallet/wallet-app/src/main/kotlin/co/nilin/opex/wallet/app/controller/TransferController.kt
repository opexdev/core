package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class TransferController(private val transferService: TransferService) {

    data class TransferBody(
        val description: String?,
        val transferRef: String?,
        val transferCategory: TransferCategory
    )

    @PostMapping("/v2/transfer/{amount}_{symbol}/from/{senderUuid}_{senderWalletType}/to/{receiverUuid}_{receiverWalletType}")
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
        @PathVariable symbol: String,
        @PathVariable senderWalletType: WalletType,
        @PathVariable senderUuid: String,
        @PathVariable receiverWalletType: WalletType,
        @PathVariable receiverUuid: String,
        @PathVariable amount: BigDecimal,
        @RequestBody transferBody: TransferBody
    ): TransferResult {
        return transferService.transfer(
            symbol,
            senderWalletType,
            senderUuid,
            receiverWalletType,
            receiverUuid,
            amount,
            transferBody.description,
            transferBody.transferRef,
            transferBody.transferCategory,
        )
    }

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
        @PathVariable symbol: String,
        @PathVariable senderWalletType: WalletType,
        @PathVariable senderUuid: String,
        @PathVariable receiverWalletType: WalletType,
        @PathVariable receiverUuid: String,
        @PathVariable amount: BigDecimal,
        @RequestParam description: String?,
        @RequestParam transferRef: String?,
        @RequestBody transferBody: TransferBody
    ): TransferResult {
        return transferService.transfer(
            symbol,
            senderWalletType,
            senderUuid,
            receiverWalletType,
            receiverUuid,
            amount,
            description,
            transferRef,
            transferBody.transferCategory
        )
    }

    @PostMapping("/deposit/{amount}_{chain}_{symbol}/{receiverUuid}_{receiverWalletType}")
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
    suspend fun deposit(
        @PathVariable symbol: String,
        @PathVariable receiverUuid: String,
        @PathVariable receiverWalletType: WalletType,
        @PathVariable amount: BigDecimal,
        @RequestParam description: String?,
        @RequestParam transferRef: String?,
        @PathVariable chain: String?
    ): TransferResult {
        return transferService.deposit(
            symbol,
            receiverUuid,
            receiverWalletType,
            amount,
            description,
            transferRef,
            chain
        )
    }

}
