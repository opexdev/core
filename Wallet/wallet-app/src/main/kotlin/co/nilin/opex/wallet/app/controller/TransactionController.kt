package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
import org.springframework.web.bind.annotation.*

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
            request.startTime,
            request.endTime,
            request.limit,
            request.offset
        )
    }

    @GetMapping("/withdraw/{uuid}")
    suspend fun getWithdrawTransactionsForUser(
        @PathVariable("uuid") uuid: String,
        @RequestBody request: TransactionRequest
    ): List<TransactionHistory> {
        return manager.findWithdrawTransactions(
            uuid,
            request.coin,
            request.startTime,
            request.endTime,
            request.limit,
            request.offset
        )
    }

}