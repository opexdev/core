package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
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

    @ApiOperation(value = "Create new referral code", notes = "Send user information to create new referral code. referentCommission is a value in range [0, 1].")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "10000",
                mediaType = "application/json"
            )
        )
    )
    @PostMapping("/codes")
    suspend fun generateReferralCode(
        @RequestBody body: PostReferralBody,
        @CurrentSecurityContext securityContext: SecurityContext
    ): String {
        if (body.uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        return referralCodeHandler.runCatching {
            val maxReferralCodePerUser = configHandler.findConfig("default")!!.maxReferralCodePerUser
            val count = referralCodeHandler.findByReferrerUuid(body.uuid).size
            if (count >= maxReferralCodePerUser) throw OpexException(
                OpexError.Forbidden,
                "You have reached maximum number of referral codes"
            )
            generateReferralCode(body.uuid, body.referentCommission)
        }
            .onFailure(badRequestOrThrow).getOrThrow()
    }

    @ApiOperation(
        value = "Update referral code",
        notes = "Edit referral code properties. The id code is immutable, you can not change it. referentCommission is a value in range [0, 1]."
    )
    @ApiResponse(message = "OK", code = 200)
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

    @ApiOperation(
        value = "Refer a user by referral code",
        notes = "Referrer can not be one of your referents. Also can not refer yourself."
    )
    @ApiResponse(message = "OK", code = 200)
    @PutMapping("/codes/{code}/assign")
    suspend fun assignReferrer(
        @PathVariable code: String,
        @RequestParam uuid: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        if (uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        referralCodeHandler.runCatching { assign(code, uuid) }.onFailure(badRequestOrThrow)
    }

    @ApiOperation(value = "Get my referral codes", notes = "Get all of your referral codes.")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "[{ \"uuid\": \"b3e4f2bd-15c6-4912-bdef-161445a98193\", \"code\": \"10000\", \"referentCommission\": 0}]",
                mediaType = "application/json"
            )
        )
    )
    @GetMapping("/me/codes")
    suspend fun getMyReferralCodes(@CurrentSecurityContext securityContext: SecurityContext): List<ReferralCodeBody> {
        return referralCodeHandler.findByReferrerUuid(securityContext.authentication.name)
            .map { ReferralCodeBody(it.uuid, it.code, it.referentCommission) }
    }

    @ApiOperation(value = "Get referral codes info", notes = "Get referral codes info.")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"uuid\": \"b3e4f2bd-15c6-4912-bdef-161445a98193\", \"code\": \"10000\", \"referentCommission\": 0}",
                mediaType = "application/json"
            )
        )
    )
    @GetMapping("/codes/{code}")
    suspend fun getReferralCodeByCode(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReferralCodeBody {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(OpexError.NotFound)
        if (referralCode.uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        return ReferralCodeBody(referralCode.uuid, referralCode.code, referralCode.referentCommission)
    }

    @ApiOperation(value = "Get referral code's references", notes = "Get uuid of all referral code's references.")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "[\"b3e4f2bd-15c6-4912-bdef-161445a98193\"]",
                mediaType = "application/json"
            )
        )
    )
    @GetMapping("/codes/{code}/references")
    suspend fun getReferenceByCode(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<String> {
        return referenceHandler.findByCode(code).map { it.referentUuid }
    }

    @ApiOperation(value = "Get all referral codes", notes = "Get all of referral codes.")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "[{ \"uuid\": \"b3e4f2bd-15c6-4912-bdef-161445a98193\", \"code\": \"10000\", \"referentCommission\": 0}]",
                mediaType = "application/json"
            )
        )
    )
    @GetMapping("/codes")
    suspend fun getAllReferralCodes(): List<ReferralCodeBody> {
        return referralCodeHandler.findAll().map { ReferralCodeBody(it.uuid, it.code, it.referentCommission) }
    }

    @ApiOperation(value = "Delete referral code", notes = "Delete referral codes by its id.")
    @ApiResponse(message = "OK", code = 200)
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
