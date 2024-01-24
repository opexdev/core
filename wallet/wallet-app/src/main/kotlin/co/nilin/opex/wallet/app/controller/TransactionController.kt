package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/transaction")
class TransactionController(private val manager: TransactionManager) {

    @PostMapping("/deposit/{uuid}")
    suspend fun getDepositTransactionsForUser(
            @PathVariable("uuid") uuid: String,
            @RequestBody request: TransactionRequest,
            @CurrentSecurityContext securityContext: SecurityContext?

    ): List<TransactionHistory> {
        if (securityContext == null || securityContext.authentication.name == uuid)
            return manager.findDepositTransactions(
                    uuid,
                    request.coin,
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault()),
                    request.limit,
                    request.offset
            )
        else
            throw OpexError.Forbidden.exception()
    }

    @PostMapping("/{uuid}")
    suspend fun getTransactionsForUser(
            @PathVariable("uuid") uuid: String,
            @RequestBody request: TransactionRequest,
            @CurrentSecurityContext securityContext: SecurityContext?
    ): List<TransactionHistory> {
        if (securityContext == null || securityContext.authentication.name == uuid)
            return manager.findTransactions(
                    uuid,
                    request.coin,
                    request.category,
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault()),
                    request.ascendingByTime,
                    request.limit,
                    request.offset
            )
        else
            throw OpexError.Forbidden.exception()
    }

}