package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.Referral
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class ReferralController {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class PostReferralBody(
        var referrerCommission: BigDecimal?,
        var referentCommission: BigDecimal?
    )

    @PostMapping("referrals")
    suspend fun generateReferralCode(@RequestBody body: PostReferralBody): String {
        TODO("Not implemented")
    }

    @PatchMapping("referrals/{code}")
    suspend fun updateReferral(@RequestBody body: PostReferralBody) {
        TODO("Not implemented")
    }

    @PutMapping("referrals/{code}/assign/{uuid}")
    suspend fun assignReferrer(@RequestParam code: String, @RequestParam uuid: String) {
        TODO("Not implemented")
    }

    @GetMapping("referrals/{code}")
    suspend fun getReferralCode(@RequestParam code: String): Referral {
        TODO("Not implemented")
    }

    @GetMapping("referrals")
    suspend fun getAllReferralCodes(): List<Referral> {
        TODO("Not implemented")
    }

    @DeleteMapping("referrals/{code}")
    suspend fun deleteReferralCode(@RequestParam code: String) {
        TODO("Not implemented")
    }
}
