package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.app.dto.UserTransactionRequest
import co.nilin.opex.wallet.core.model.TradeAdminResponse
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.core.spi.UserTransactionManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/admin/v2/transaction")
class TransactionAdminController(
    private val manager: UserTransactionManager,
    private val transactionManager: TransactionManager
) {

    @Deprecated("endpoint changed")
    @PostMapping
    suspend fun getUserTransactions(
        principal: Principal,
        @RequestBody request: UserTransactionRequest
    ): List<UserTransactionHistory> {
        return with(request) {
            manager.getTransactionHistory(
                userId,
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

    @PostMapping("/history")
    suspend fun getUserTransactionsHistory(
        @RequestBody request: UserTransactionRequest
    ): List<UserTransactionHistory> {
        return with(request) {
            manager.getTransactionHistory(
                userId,
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

    // This part is temporary and the structure of fetching trades needs to be fixed.
    @PostMapping("/trades")
    suspend fun getTradeHistory(@RequestBody request: TransactionRequest): List<TradeAdminResponse> {
        return transactionManager.findTradesForAdmin(
            request.coin,
            request.startTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
            request.endTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
            request.ascendingByTime,
            request.limit ?: 10,
            request.offset ?: 0
        )
    }


}