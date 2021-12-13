package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.Referral
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class ReferralController(private val referralCodeHandler: ReferralCodeHandler) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class PostReferralBody(
        var referrerCommission: BigDecimal,
        var referentCommission: BigDecimal
    )

    @PostMapping("/referrals")
    suspend fun generateReferralCode(@RequestBody body: PostReferralBody): String {
        return referralCodeHandler.generateReferralCode("", body.referrerCommission, body.referentCommission)
    }

    @PatchMapping("/referrals/{code}")
    suspend fun updateReferralCodeByCode(@PathVariable code: String, @RequestBody body: PostReferralBody) {
        referralCodeHandler.updateCommissions(code, body.referrerCommission, body.referentCommission)
    }

    @PutMapping("/referrals/{code}/assign/{uuid}")
    suspend fun assignReferrer(@PathVariable code: String, @PathVariable uuid: String) {
        referralCodeHandler.assign(code, uuid)
    }

    @GetMapping("/referrals/{code}")
    suspend fun getReferralCodeByCode(@PathVariable code: String): Referral? {
        return referralCodeHandler.findReferralByCode(code)
    }

    @GetMapping("/referrals")
    suspend fun getAllReferralCodes(): List<Referral> {
        return referralCodeHandler.findAllReferrals()
    }

    @DeleteMapping("/referrals/{code}")
    suspend fun deleteReferralCode(@PathVariable code: String) {
        referralCodeHandler.deleteReferralCode(code)
    }
}
