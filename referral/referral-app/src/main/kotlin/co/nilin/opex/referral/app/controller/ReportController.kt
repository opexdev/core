package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class ReportController(
    private val commissionRewardHandler: CommissionRewardHandler,
    private val referenceHandler: ReferenceHandler,
    private val referralCodeHandler: ReferralCodeHandler
) {
    data class ReferralCodeReportBody(
        val code: String,
        val referentsCount: Long,
        val share: BigDecimal
    )

    data class ReferrerReportBody(
        val referentsCount: Long,
        val share: BigDecimal
    )

    @ApiOperation(
        value = "Get report by referral code",
        notes = "Get report by referral code."
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        response = ReferralCodeReportBody::class,
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = """
{
    "code": "10000",
    "referentsCount": 1,
    "share": 0.001
}
                """,
            )
        )
    )
    @GetMapping("/reports/codes/{code}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getReportByCode(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReferralCodeReportBody {
        val referralCode = referralCodeHandler.findByCode(code) ?: throw OpexException(OpexError.NotFound)
        if (referralCode.uuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        val referencesCount = referenceHandler.findByCode(code).size.toLong()
        val commissions = commissionRewardHandler.findCommissions(referralCode = code)
        return ReferralCodeReportBody(code, referencesCount, commissions.sumOf { it.share })
    }

    @ApiOperation(
        value = "Get report by uuid",
        notes = "Get report by uuid."
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        response = ReferrerReportBody::class,
        examples = Example(
            ExampleProperty(
                mediaType = "application/json",
                value = """
{
    "referentsCount": 1,
    "share": 0.001
}
                """,
            )
        )
    )
    @GetMapping("/reports/users/{rewardedUuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getReportByRewardedUuid(
        @PathVariable rewardedUuid: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReferrerReportBody {
        if (rewardedUuid != securityContext.authentication.name) throw OpexException(OpexError.UnAuthorized)
        val referencesCount = referenceHandler.findByReferrerUuid(rewardedUuid).size.toLong()
        val commissions = commissionRewardHandler.findCommissions(rewardedUuid = rewardedUuid)
        return ReferrerReportBody(referencesCount, commissions.sumOf { it.share })
    }
}
