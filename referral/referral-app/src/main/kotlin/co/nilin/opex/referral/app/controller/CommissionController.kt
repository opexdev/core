package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class CommissionController(private val commissionRewardHandler: CommissionRewardHandler) {
    data class CommissionRewardBody(
        var referrerUuid: String,
        var referentUuid: String,
        var referralCode: String,
        var richTrade: Long,
        var referrerShare: BigDecimal,
        var referentShare: BigDecimal,
    )

    @GetMapping("/commissions/{code}")
    suspend fun getCommissionsByReferrerAndCode(
        @PathVariable code: String
    ): List<CommissionRewardBody> {
        return commissionRewardHandler.findCommissions(referralCode = code).map {
            CommissionRewardBody(
                it.referrerUuid,
                it.referentUuid,
                it.referralCode,
                it.richTrade.first,
                it.referrerShare,
                it.referentShare
            )
        }
    }

    @GetMapping("/commissions")
    suspend fun getCommissionsByReferent(
        @RequestParam referrerUuid: String?,
        @RequestParam referentUuid: String?
    ): List<CommissionRewardBody> {
        return commissionRewardHandler.findCommissions(referentUuid = referentUuid, referrerUuid = referrerUuid).map {
            CommissionRewardBody(
                it.referrerUuid,
                it.referentUuid,
                it.referralCode,
                it.richTrade.first,
                it.referrerShare,
                it.referentShare
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
