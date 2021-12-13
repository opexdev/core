package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.CommissionReward
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import org.springframework.web.bind.annotation.*

@RestController
class CommissionController(private val referralCodeHandler: ReferralCodeHandler) {
    @GetMapping("/commissions")
    suspend fun getCommissionsByUuidAndCode(
        @RequestParam code: String,
        @RequestParam uuid: String
    ): List<CommissionReward> {
        TODO("Not yet implemented")
    }

    @DeleteMapping("/commissions")
    suspend fun deleteCommissions(@RequestParam code: String, @RequestParam uuid: String) {
        TODO("Not yet implemented")
    }

    @DeleteMapping("/commissions/{id}")
    suspend fun deleteCommissionsById(@PathVariable id: String) {
        TODO("Not yet implemented")
    }
}
