package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import org.springframework.web.bind.annotation.*

@RestController
class CommissionController(private val commissionRewardHandler: CommissionRewardHandler) {
    @GetMapping("/commissions")
    suspend fun getCommissionsByReferrerAndCode(
        @RequestParam uuid: String,
        @RequestParam code: String?
    ): List<CommissionReward> {
        return commissionRewardHandler.findCommissionsByReferrer(uuid, code)
    }

    @DeleteMapping("/commissions")
    suspend fun deleteCommissions(
        @RequestParam code: String?,
        @RequestParam referrerUuid: String?,
        @RequestParam referentUuid: String?
    ) {
        if (code != null) commissionRewardHandler.deleteCommissionsByCode(code)
        else if (referrerUuid != null) commissionRewardHandler.deleteCommissionsByReferrer(referrerUuid)
        else if (referentUuid != null) commissionRewardHandler.deleteCommissionsByReferent(referentUuid)
        else commissionRewardHandler.deleteAllCommissions()
    }

    @DeleteMapping("/commissions/{id}")
    suspend fun deleteCommissionById(@PathVariable id: Long) {
        commissionRewardHandler.deleteCommissionById(id)
    }
}
