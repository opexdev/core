package co.nilin.opex.accountant.app.controller

import co.nilin.opex.accountant.core.api.DepositActivityManager
import co.nilin.opex.accountant.core.api.TradeActivityManager
import co.nilin.opex.accountant.core.api.WithdrawActivityManager
import co.nilin.opex.accountant.core.model.DailyAmount
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user-activity")
class UserDailyActivityController(
    private val withdrawManager: WithdrawActivityManager,
    private val depositManager: DepositActivityManager,
    private val TradeManager: TradeActivityManager
) {

    @GetMapping("/withdraw/{userId}")
    suspend fun getDailyWithdrawLast31Days(
        @PathVariable userId: String
    ): List<DailyAmount> {
        return withdrawManager.getLastDaysWithdrawActivity(
            userId = userId
        )
    }

    @GetMapping("/deposit/{userId}")
    suspend fun getDailyDepositLast31Days(
        @PathVariable userId: String
    ): List<DailyAmount> {
        return depositManager.getLastDaysDepositActivity(
            userId = userId
        )
    }

    @GetMapping("/trade/{userId}")
    suspend fun getDailyTradeLast31Days(
        @PathVariable userId: String
    ): List<DailyAmount> {
        return TradeManager.getLastDaysTradeActivity(
            userId = userId
        )
    }
}