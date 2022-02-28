package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class ReferralController(
    private val referralCodeHandler: ReferralCodeHandler,
    private val referenceHandler: ReferenceHandler,
    private val configHandler: ConfigHandler
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

    private val badRequestOrThrow: (Throwable) -> Unit = { e ->
        when (e) {
            is IllegalArgumentException -> throw OpexException(OpexError.BadRequest, e.message)
            is OpexException -> throw e
            else -> throw OpexException(OpexError.InternalServerError, e.message)
        }
    }

    @PostMapping("/codes")
    suspend fun generateReferralCode(
        @RequestBody body: PostReferralBody,
        @CurrentSecurityContext securityContext: SecurityContext
    ): String {
        if (body.uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        return referralCodeHandler.runCatching {
            val maxReferralCodePerUser = configHandler.findConfig("default")!!.maxReferralCodePerUser
            val count = referralCodeHandler.findByReferrerUuid(body.uuid).size
            if (count > maxReferralCodePerUser) throw OpexException(
                OpexError.Forbidden,
                "You have reached maximum number of referral codes"
            )
            generateReferralCode(body.uuid, body.referentCommission)
        }
            .onFailure(badRequestOrThrow).getOrThrow()
    }

    @PatchMapping("/codes/{code}")
    suspend fun updateReferralCodeByCode(
        @PathVariable code: String,
        @RequestBody body: PatchReferralBody,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(
            OpexError.BadRequest,
            "Referral code is invalid"
        )
        if (referralCode.uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        referralCodeHandler.runCatching { updateCommissions(code, body.referentCommission) }
            .onFailure(badRequestOrThrow)
    }

    @PutMapping("/codes/{code}/assign")
    suspend fun assignReferrer(
        @PathVariable code: String,
        @RequestParam uuid: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        if (uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        referralCodeHandler.runCatching { assign(code, uuid) }.onFailure(badRequestOrThrow)
    }

    @GetMapping("/me/codes")
    suspend fun getMyReferralCodes(@CurrentSecurityContext securityContext: SecurityContext): List<ReferralCodeBody> {
        return referralCodeHandler.findByReferrerUuid(securityContext.authentication.name)
            .map { ReferralCodeBody(it.uuid, it.code, it.referentCommission) }
    }

    @GetMapping("/codes/{code}")
    suspend fun getReferralCodeByCode(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReferralCodeBody {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(OpexError.NotFound)
        if (referralCode.uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        return ReferralCodeBody(referralCode.uuid, referralCode.code, referralCode.referentCommission)
    }

    @GetMapping("/codes/{code}/references")
    suspend fun getReferenceByCode(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<String> {
        return referenceHandler.findByCode(code).map { it.referentUuid }
    }

    @GetMapping("/codes")
    suspend fun getAllReferralCodes(): List<ReferralCodeBody> {
        return referralCodeHandler.findAll().map { ReferralCodeBody(it.uuid, it.code, it.referentCommission) }
    }

    @DeleteMapping("/codes/{code}")
    suspend fun deleteReferralCode(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(OpexError.NotFound)
        if (referralCode.uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        referralCodeHandler.deleteByCode(code)
    }
}
