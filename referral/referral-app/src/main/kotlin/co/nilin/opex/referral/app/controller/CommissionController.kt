package co.nilin.opex.referral.app.controller

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.ZoneId
import java.util.*

@RestController
class CommissionController(private val commissionRewardHandler: CommissionRewardHandler) {
    data class CommissionRewardBody(
        var rewardedUuid: String,
        var referentUuid: String,
        var referralCode: String,
        var richTrade: Long,
        var referentOrderDirection: OrderDirection,
        var share: BigDecimal,
        var createDate: Date
    )

    @GetMapping("/commissions/{code}")
    suspend fun getCommissionsByReferrerAndCode(
        @PathVariable code: String
    ): List<CommissionRewardBody> {
        return commissionRewardHandler.findCommissions(referralCode = code).map {
            CommissionRewardBody(
                it.rewardedUuid,
                it.referentUuid,
                it.referralCode,
                it.richTrade.first,
                it.referentOrderDirection,
                it.share,
                Date.from(it.createDate.atZone(ZoneId.systemDefault()).toInstant())
            )
        }
    }

    @GetMapping("/commissions")
    suspend fun getCommissionsByReferent(
        @RequestParam rewardedUuid: String?,
        @RequestParam referentUuid: String?
    ): List<CommissionRewardBody> {
        return commissionRewardHandler.findCommissions(referentUuid = referentUuid, rewardedUuid = rewardedUuid).map {
            CommissionRewardBody(
                it.rewardedUuid,
                it.referentUuid,
                it.referralCode,
                it.richTrade.first,
                it.referentOrderDirection,
                it.share,
                Date.from(it.createDate.atZone(ZoneId.systemDefault()).toInstant())
            )
        }
    }

    @DeleteMapping("/commissions")
    suspend fun deleteCommissions(
        @RequestParam code: String?,
        @RequestParam referrerUuid: String?,
        @RequestParam referentUuid: String?
    ) {
        commissionRewardHandler.deleteCommissions(code, referrerUuid, referentUuid)
    }

    @DeleteMapping("/commissions/{id}")
    suspend fun deleteCommissionById(@PathVariable id: Long) {
        commissionRewardHandler.deleteCommissionById(id)
    }
}
