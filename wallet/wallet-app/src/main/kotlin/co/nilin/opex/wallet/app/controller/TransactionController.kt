package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.UserTransactionRequest
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import co.nilin.opex.wallet.core.spi.UserTransactionManager
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/v2/transaction")
class TransactionController(private val manager: UserTransactionManager) {

    @PostMapping("/{uuid}")
    suspend fun getUserTransactions(
        @PathVariable uuid: String,
        @RequestBody request: UserTransactionRequest
    ): List<UserTransactionHistory> {
        return with(request) {
            manager.getTransactionHistoryForUser(
                uuid,
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

}