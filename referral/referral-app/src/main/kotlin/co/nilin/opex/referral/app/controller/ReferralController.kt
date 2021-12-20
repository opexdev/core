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
        var uuid: String,
        var referentCommission: BigDecimal
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class PatchReferralBody(
        var referentCommission: BigDecimal
    )

    @PostMapping("/codes")
    suspend fun generateReferralCode(@RequestBody body: PostReferralBody): String {
        return referralCodeHandler.generateReferralCode(
            body.uuid,
            body.referentCommission
        )
    }

    @PatchMapping("/codes/{code}")
    suspend fun updateReferralCodeByCode(@PathVariable code: String, @RequestBody body: PatchReferralBody) {
        referralCodeHandler.updateCommissions(code, body.referentCommission)
    }

    @PutMapping("/codes/{code}/assign")
    suspend fun assignReferrer(@PathVariable code: String, @RequestParam uuid: String) {
        referralCodeHandler.assign(code, uuid)
    }

    @GetMapping("/codes/{code}")
    suspend fun getReferralCodeByCode(@PathVariable code: String): ReferralCode? {
        return referralCodeHandler.findByCode(code)
    }

    @GetMapping("/codes")
    suspend fun getAllReferralCodes(): List<ReferralCode> {
        return referralCodeHandler.findAll()
    }

    @DeleteMapping("/codes/{code}")
    suspend fun deleteReferralCode(@PathVariable code: String) {
        referralCodeHandler.deleteByCode(code)
    }
}
