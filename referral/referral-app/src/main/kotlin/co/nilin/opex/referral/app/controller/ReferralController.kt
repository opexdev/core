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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class PatchReferralBody(
        var referrerCommission: BigDecimal?,
        var referentCommission: BigDecimal?
    )

    @PostMapping("/")
    suspend fun generateReferralCode(@RequestBody body: PostReferralBody): String {
        return referralCodeHandler.generateReferralCode("", body.referrerCommission, body.referentCommission)
    }

    @PatchMapping("/{code}")
    suspend fun updateReferral(@RequestParam code: String, @RequestBody body: PatchReferralBody) {
        referralCodeHandler.updateCommissions(code, body.referrerCommission, body.referentCommission)
    }

    @PutMapping("/{code}/assign/{uuid}")
    suspend fun assignReferrer(@RequestParam code: String, @RequestParam uuid: String) {
        referralCodeHandler.assign(code, uuid)
    }

    @GetMapping("/{code}")
    suspend fun getReferralCode(@RequestParam code: String): Referral? {
        return referralCodeHandler.findReferralByCode(code)
    }

    @GetMapping("/")
    suspend fun getAllReferralCodes(): List<Referral> {
        return referralCodeHandler.findAllReferrals()
    }

    @DeleteMapping("/{code}")
    suspend fun deleteReferralCode(@RequestParam code: String) {
        referralCodeHandler.deleteReferralCode(code)
    }
}
