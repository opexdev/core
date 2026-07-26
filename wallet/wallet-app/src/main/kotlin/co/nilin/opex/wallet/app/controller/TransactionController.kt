package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.UserTransactionRequest
import co.nilin.opex.wallet.app.utils.asLocalDateTime
import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import co.nilin.opex.wallet.core.spi.ReservedTransferManager
import co.nilin.opex.wallet.core.spi.UserTransactionManager
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/v2/transaction")
class TransactionController(
    private val manager: UserTransactionManager,
    private val reservedTransferManager: ReservedTransferManager,

    ) {

    @PostMapping
    suspend fun getUserTransactions(
        principal: Principal,
        @RequestBody request: UserTransactionRequest,
    ): List<UserTransactionHistory> {
        return with(request) {
            manager.getTransactionHistory(
                principal.name,
                currency,
                category,
                startTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
                endTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
                ascendingByTime,
                limit ?: 10,
                offset ?: 0
            )
        }
    }

    @PostMapping("/count")
    suspend fun getUserTransactionsCount(
        principal: Principal,
        @RequestBody request: UserTransactionRequest,
    ): Long {
        return with(request) {
            manager.getTransactionHistoryCount(
                principal.name,
                currency,
                category,
                startTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
                endTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
            )
        }
    }

    @GetMapping("/trade/summary/{uuid}")
    suspend fun getUserTradeTransactionSummary(
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @PathVariable uuid: String,
    ): List<TransactionSummary> {
        return manager.getTradeTransactionSummary(
            uuid,
            startTime?.asLocalDateTime(),
            endTime?.asLocalDateTime(),
            limit,
        )
    }

}