package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.model.TransactionWithDetailHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
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
        @RequestBody request: TransactionRequest
    ): List<TransactionHistory> {
        return manager.findDepositTransactions(
            uuid,
            request.coin,
            request.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault())
            },
            request.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault())
            },
            request.limit!!,
            request.offset!!,
                request.ascendingByTime?:false
        )
    }

    @PostMapping("/{uuid}")
    suspend fun getTransactionsForUser(
        @PathVariable("uuid") uuid: String,
        @RequestBody request: TransactionRequest
    ): List<TransactionWithDetailHistory> {
        return manager.findTransactions(
            uuid,
            request.coin,
            request.category,
            request.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault())
            },
            request.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault())
            },
            request.ascendingByTime,
            request.limit!!,
            request.offset!!
        )
    }

}