package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.UserTransactionRequest
import co.nilin.opex.wallet.core.model.UserTransactionHistory
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
class TransactionAdminController(private val manager: UserTransactionManager) {

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

}