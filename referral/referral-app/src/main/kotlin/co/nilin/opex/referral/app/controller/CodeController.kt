package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.ConfigHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import com.nimbusds.jose.shaded.json.JSONArray
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal

@RestController
@RequestMapping("/codes")
class CodeController(
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

    private val reThrow: (Throwable) -> Unit = { e ->
        when (e) {
            is IllegalArgumentException -> throw OpexException(OpexError.BadRequest, e.message)
            is OpexException -> throw e
            else -> throw OpexException(OpexError.InternalServerError, e.message)
        }
    }

    @ApiOperation(
        value = "Create new referral code",
        notes = "Send user information to create new referral code. referentCommission is a value in range [0, 1]."
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        response = String::class,
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = "10000"
            )
        )
    )
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun generateReferralCode(
        @RequestBody body: PostReferralBody,
        principal: Principal
    ): String {
        if (body.uuid != principal.name) throw OpexException(OpexError.UnAuthorized)
        return referralCodeHandler.runCatching {
            val maxReferralCodePerUser = configHandler.findConfig("default")!!.maxReferralCodePerUser
            val count = referralCodeHandler.findByReferrerUuid(body.uuid).size
            if (count >= maxReferralCodePerUser) throw OpexException(
                OpexError.Forbidden,
                "You have reached maximum number of referral codes"
            )
            generateReferralCode(body.uuid, body.referentCommission)
        }.onFailure(reThrow).getOrThrow()
    }

    @ApiOperation(
        value = "Update referral code",
        notes = "Edit referral code properties. The id code is immutable, you can not change it. referentCommission is a value in range [0, 1]."
    )
    @ApiResponse(message = "OK", code = 200)
    @PatchMapping("/{code}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun updateReferralCodeByCode(
        @PathVariable code: String,
        @RequestBody body: PatchReferralBody,
        principal: Principal
    ) {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(
            OpexError.BadRequest,
            "Referral code is invalid"
        )
        if (referralCode.uuid != principal.name) throw OpexException(OpexError.UnAuthorized)
        referralCodeHandler.runCatching { updateCommissions(code, body.referentCommission) }
            .onFailure(reThrow)
    }

    @ApiOperation(value = "Get referral codes info", notes = "Get referral codes info.")
    @ApiResponse(
        message = "OK",
        code = 200,
        response = ReferralCodeBody::class,
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = """
{ 
    "uuid": "b3e4f2bd-15c6-4912-bdef-161445a98193",
    "code": "10000", 
    "referentCommission": 0
}
                """,
            )
        )
    )
    @GetMapping("/{code}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getReferralCodeByCode(
        @PathVariable code: String,
        principal: Principal
    ): ReferralCodeBody {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(OpexError.NotFound)
        if (referralCode.uuid != principal.name) throw OpexException(OpexError.UnAuthorized)
        return ReferralCodeBody(referralCode.uuid, referralCode.code, referralCode.referentCommission)
    }

    @ApiOperation(value = "Get all referral codes", notes = "Get all of referral codes.")
    @ApiResponse(
        message = "OK",
        code = 200,
        response = ReferralCodeBody::class,
        responseContainer = "List",
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = """
[
    {
        "uuid": "b3e4f2bd-15c6-4912-bdef-161445a98193", 
        "code": "10000", 
        "referentCommission": 0
    }
]
                """,
            )
        )
    )
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getAllReferralCodes(
        @RequestParam uuid: String?,
        principal: Principal
    ): List<ReferralCodeBody> {
        return uuid?.takeIf { uuid == principal.name }?.let { id ->
            referralCodeHandler.findByReferrerUuid(id).map { ReferralCodeBody(it.uuid, it.code, it.referentCommission) }
        } ?: run {
            val isAdmin = ((principal as Jwt).claims["roles"] as? JSONArray)?.contains("finance-admin") ?: false
            return if (isAdmin) referralCodeHandler.findAll()
                .map { ReferralCodeBody(it.uuid, it.code, it.referentCommission) }
            else throw OpexException(OpexError.UnAuthorized)
        }
    }

    @ApiOperation(value = "Delete referral code", notes = "Delete referral codes by its id.")
    @ApiResponse(message = "OK", code = 200)
    @DeleteMapping("/{code}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun deleteReferralCode(
        @PathVariable code: String,
        principal: Principal
    ) {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(OpexError.NotFound)
        if (referralCode.uuid != principal.name) throw OpexException(OpexError.UnAuthorized)
        referralCodeHandler.deleteByCode(code)
    }
}
