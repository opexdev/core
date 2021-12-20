package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.Reference
import co.nilin.opex.referral.core.model.ReferralCode
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class ReferralController(private val referralCodeHandler: ReferralCodeHandler, private val referenceHandler: ReferenceHandler) {
    data class PostReferralBody(
        var uuid: String,
        var referentCommission: BigDecimal
    )

    data class PatchReferralBody(
        var referentCommission: BigDecimal
    )

    @PostMapping("/codes")
    suspend fun generateReferralCode(@RequestBody body: PostReferralBody): String {
        return referralCodeHandler.generateReferralCode(body.uuid, body.referentCommission)
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

    @GetMapping("/codes/{code}/references")
    suspend fun getReferenceByCode(@PathVariable code: String): List<Reference>? {
        return referenceHandler.findByCode(code)
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
