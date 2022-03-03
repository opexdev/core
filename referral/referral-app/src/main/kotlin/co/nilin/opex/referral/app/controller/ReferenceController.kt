package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.model.Reference
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class ReferenceController(
    private val referralCodeHandler: ReferralCodeHandler,
    private val referenceHandler: ReferenceHandler
) {
    data class ReferenceBody(
        var referralCode: String,
        var referentUuid: String,
    )

    private val reThrow: (Throwable) -> Unit = { e ->
        when (e) {
            is IllegalArgumentException -> throw OpexException(OpexError.BadRequest, e.message)
            is OpexException -> throw e
            else -> throw OpexException(OpexError.InternalServerError, e.message)
        }
    }

    @ApiOperation(
        value = "Refer a user by referral code",
        notes = "Referrer can not be one of your referents. Also you can not refer yourself."
    )
    @ApiResponse(message = "OK", code = 200)
    @PutMapping("/references/assign", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun assignReferrer(
        @RequestParam code: String,
        @RequestParam(required = false) uuid: String?,
        principal: Principal
    ) {
        val id = uuid ?: principal.name
        if (id != principal.name) throw OpexException(OpexError.UnAuthorized)
        referralCodeHandler.runCatching { assign(code, id) }.onFailure(reThrow)
    }

    @ApiOperation(value = "Get referral code's references", notes = "Get uuid of all referral code's references.")
    @ApiResponse(
        message = "OK",
        code = 200,
        response = String::class,
        responseContainer = "List",
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = """
[
    "b3e4f2bd-15c6-4912-bdef-161445a98193"
]
                """,
            )
        )
    )
    @GetMapping("/references", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getReferenceByCodeAndUuid(
        @RequestParam(required = false) uuid: String?,
        @RequestParam(required = false) code: String?,
        principal: Principal
    ): List<ReferenceBody> {
        fun List<Reference>.res() = map { ReferenceBody(it.referralCode.code, it.referentUuid) }
        return when (uuid ?: code) {
            null -> throw OpexException(OpexError.BadRequest, "One of (uuid, code) parameters must be provided")
            uuid -> referenceHandler.findByReferrerUuid(uuid)
                .let { it.takeIf { code == null } ?: it.filter { v -> v.referralCode.code == code } }.res()
            code -> referenceHandler.findByCode(code).res()
            else -> throw OpexException(OpexError.InternalServerError, "All of (code, uuid) are null")
        }
    }
}
