package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.AdminSearchDepositRequest
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.service.DepositService
import co.nilin.opex.wallet.core.inout.DepositAdminResponse
import co.nilin.opex.wallet.core.inout.TerminalCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.spi.TerminalManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@RestController
@RequestMapping("/admin/deposit")

class DepositAdminController(
    private val depositService: DepositService,
    private val terminalManager: TerminalManager
) {


    @PostMapping("/manually/{amount}_{symbol}/{receiverUuid}")
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
    suspend fun depositManually(
        @PathVariable("symbol") symbol: String,
        @PathVariable("receiverUuid") receiverUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult? {
        return depositService.depositManually(
            symbol, receiverUuid,
            securityContext.authentication.name, amount, request
        )
    }

    @PostMapping("/search")
    suspend fun search(
        @RequestParam offset: Int,
        @RequestParam size: Int,
        @RequestBody body: AdminSearchDepositRequest
    ): List<DepositAdminResponse> {
        return depositService.searchDeposit(
            body.uuid,
            body.currency,
            body.sourceAddress,
            body.transactionRef,
            body.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.startTime), ZoneId.systemDefault())
            },
            body.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.endTime), ZoneId.systemDefault())
            },
            body.status,
            offset,
            size,
            body.ascendingByTime,
        )
    }


    @PostMapping("/terminal")
    suspend fun registerTerminal(
        @RequestBody body: TerminalCommand
    ): TerminalCommand? {
        return terminalManager.save(body.apply { uuid = UUID.randomUUID().toString() })
    }


    @PutMapping("/terminal/{uuid}")
    suspend fun updateTerminal(
        @PathVariable("uuid") terminalUuid: String,
        @RequestBody body: TerminalCommand
    ): TerminalCommand? {
        return terminalManager.update(body.apply { uuid = terminalUuid })
    }


    @DeleteMapping("/terminal/{uuid}")
    suspend fun deleteTerminal(
        @PathVariable("uuid") terminalUuid: String,
    ) {
        terminalManager.delete(terminalUuid)
    }

    @GetMapping("/terminal")
    suspend fun getTerminal(
    ): List<TerminalCommand>? {
        return terminalManager.fetchTerminal()
    }

    @GetMapping("/terminal/{uuid}")
    suspend fun getTerminal(
        @PathVariable("uuid") terminalUuid: String,
    ) {
        terminalManager.fetchTerminal(terminalUuid)
    }


}