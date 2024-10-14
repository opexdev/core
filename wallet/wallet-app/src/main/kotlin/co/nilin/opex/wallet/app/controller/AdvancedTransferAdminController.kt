package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.*
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.ReservedTransferManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
class AdvancedTransferAdminController {

    @Autowired
    lateinit var reservedTransferManager: ReservedTransferManager


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
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest

    ): List<ReservedTransfer>? {
        return with(request) {
            reservedTransferManager.findByCriteria(
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
}
