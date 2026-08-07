package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.AdminTransferReserveRequest
import co.nilin.opex.wallet.app.dto.ReservedTransferResponse
import co.nilin.opex.wallet.app.dto.UserSwapTransactionRequest
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.AdminSwapResponse
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.ReservedTransferManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
class AdvancedTransferAdminController {

    @Autowired
    lateinit var reservedTransferManager: ReservedTransferManager

    @Autowired
    lateinit var transferService: TransferService

    @PostMapping("/admin/v1/swap/history")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{}",
                mediaType = "application/json"
            )
        )
    )
    suspend fun getSwapHistory(
        @RequestBody request: UserSwapTransactionRequest

    ): List<AdminSwapResponse>? {
        return with(request) {
            reservedTransferManager.findByCriteriaForAdmin(
                userId,
                sourceSymbol,
                destSymbol,
                startTime?.let {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it),
                        ZoneId.systemDefault()
                    )
                },
                endTime?.let {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it),
                        ZoneId.systemDefault()
                    )
                },
                limit ?: 10,
                offset ?: 0,
                ascendingByTime,
                status
            )
        }
    }

    @PostMapping("/admin/v1/transfer/reserve")
    suspend fun reserve(@RequestBody request: AdminTransferReserveRequest): ReservedTransferResponse {
        return transferService.reserveTransferByAdmin(
            request.sourceSymbol,
            request.destSymbol,
            request.receiverUuid,
            WalletType.MAIN,
            request.receiverUuid,
            WalletType.MAIN,
            request.sourceAmount,
            request.destAmount,
            request.rate
        )
    }

    @PostMapping("/admin/v1/transfer/{reserveUuid}")
    suspend fun finalizeTransfer(
        @PathVariable reserveUuid: String,
        @RequestParam description: String?,
        @RequestParam transferRef: String?,
    ): TransferResult {
        return transferService.advanceTransferByAdmin(
            reserveUuid,
            description,
            transferRef
        ).transferResult
    }
}
