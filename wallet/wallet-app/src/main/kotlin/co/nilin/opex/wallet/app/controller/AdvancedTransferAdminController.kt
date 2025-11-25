package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.UserTransactionRequest
import co.nilin.opex.wallet.core.inout.AdminSwapResponse
import co.nilin.opex.wallet.core.spi.ReservedTransferManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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
}
