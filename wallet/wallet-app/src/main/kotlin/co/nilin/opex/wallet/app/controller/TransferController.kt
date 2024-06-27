package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.dto.TransferRequest
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.TransferResult
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class TransferController(private val transferService: TransferService) {
    data class TransferBody(
            val description: String?,
            val transferRef: String?,
            val transferCategory: String,
            val additionalData: Map<String, Any>?
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
            @PathVariable("symbol") symbol: String,
            @PathVariable("senderWalletType") senderWalletType: String,
            @PathVariable("senderUuid") senderUuid: String,
            @PathVariable("receiverWalletType") receiverWalletType: String,
            @PathVariable("receiverUuid") receiverUuid: String,
            @PathVariable("amount") amount: BigDecimal,
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
                transferBody.additionalData
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
            @PathVariable("symbol") symbol: String,
            @PathVariable("senderWalletType") senderWalletType: String,
            @PathVariable("senderUuid") senderUuid: String,
            @PathVariable("receiverWalletType") receiverWalletType: String,
            @PathVariable("receiverUuid") receiverUuid: String,
            @PathVariable("amount") amount: BigDecimal,
            @RequestParam("description") description: String?,
            @RequestParam("transferRef") transferRef: String?,
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
                transferBody.transferCategory,
                transferBody.additionalData
        )
    }

    @PostMapping("/transfer/batch")
    suspend fun batchTransfer(@RequestBody request: List<TransferRequest>) {
        transferService.batchTransfer(request)
    }


}
