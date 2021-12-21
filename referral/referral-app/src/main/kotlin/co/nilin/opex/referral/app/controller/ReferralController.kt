package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class ReferralController(
    private val referralCodeHandler: ReferralCodeHandler,
    private val referenceHandler: ReferenceHandler
) {
    data class PostReferralBody(
        val uuid: String,
        val referentCommission: BigDecimal
    )

    data class PatchReferralBody(
        val referentCommission: BigDecimal
    )

    data class ReferralCodeBody(
        val uuid: String,
        val code: String,
        val referentCommission: BigDecimal
    )

    data class ReferenceBody(
        var referralCode: ReferralCodeBody,
        var referentUuid: String,
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
    suspend fun getReferralCodeByCode(@PathVariable code: String): ReferralCodeBody {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(OpexError.NotFound)
        return ReferralCodeBody(referralCode.uuid, referralCode.code, referralCode.referentCommission)
    }

    @GetMapping("/codes/{code}/references")
    suspend fun getReferenceByCode(@PathVariable code: String): List<ReferenceBody> {
        return referenceHandler.findByCode(code).map {
            val referralCode =
                ReferralCodeBody(it.referralCode.uuid, it.referralCode.code, it.referralCode.referentCommission)
            ReferenceBody(referralCode, it.referentUuid)
        }
    }

    @GetMapping("/codes")
    suspend fun getAllReferralCodes(): List<ReferralCodeBody> {
        return referralCodeHandler.findAll().map { ReferralCodeBody(it.uuid, it.code, it.referentCommission) }
    }

    @DeleteMapping("/codes/{code}")
    suspend fun deleteReferralCode(@PathVariable code: String) {
        referralCodeHandler.deleteByCode(code)
    }
}
