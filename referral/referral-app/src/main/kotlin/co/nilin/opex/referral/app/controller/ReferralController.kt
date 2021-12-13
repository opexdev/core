package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.ReferralCode
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

    @PostMapping("/codes")
    suspend fun generateReferralCode(@RequestBody body: PostReferralBody): String {
        return referralCodeHandler.generateReferralCode("<uuid of authenticated user>", body.referrerCommission, body.referentCommission)
    }

    @PatchMapping("/codes/{code}")
    suspend fun updateReferralCodeByCode(@PathVariable code: String, @RequestBody body: PostReferralBody) {
        referralCodeHandler.updateCommissions(code, body.referrerCommission, body.referentCommission)
    }

    @PutMapping("/codes/{code}/assign")
    suspend fun assignReferrer(@PathVariable code: String, @RequestPart uuid: String) {
        referralCodeHandler.assign(code, uuid)
    }

    @GetMapping("/codes/{code}")
    suspend fun getReferralCodeByCode(@PathVariable code: String): ReferralCode? {
        return referralCodeHandler.findReferralCodeByCode(code)
    }

    @GetMapping("/codes")
    suspend fun getAllReferralCodes(): List<ReferralCode> {
        return referralCodeHandler.findAllReferralCodes()
    }

    @DeleteMapping("/codes/{code}")
    suspend fun deleteReferralCode(@PathVariable code: String) {
        referralCodeHandler.deleteReferralCodeByCode(code)
    }
}
