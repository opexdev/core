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
        @RequestParam uuid: String?,
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
        @RequestParam uuid: String?,
        @RequestParam code: String?,
        principal: Principal
    ): List<ReferenceBody> {
        if ((uuid ?: code == null) || (uuid != null && code != null)) throw OpexException(
            OpexError.BadRequest,
            "One and only one of (uuid, code) parameters must be provided"
        )
        if (uuid != null && uuid != principal.name) throw OpexException(OpexError.UnAuthorized)
        code?.let {
            val referralCode = referralCodeHandler.findByCode(it) ?: throw OpexException(OpexError.NotFound)
            if (referralCode.uuid != principal.name) throw OpexException(OpexError.UnAuthorized)
        }
        fun List<Reference>.body() = map { ReferenceBody(it.referralCode.code, it.referentUuid) }
        return when (code ?: uuid) {
            code -> referenceHandler.findByCode(code!!).body()
            uuid -> referenceHandler.findByReferrerUuid(uuid!!).body()
            else -> throw IllegalStateException("All of (code, uuid) are null")
        }
    }
}
