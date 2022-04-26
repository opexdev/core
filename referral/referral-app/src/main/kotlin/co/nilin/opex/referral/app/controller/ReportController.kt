package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal

@RestController
class ReportController(
    private val commissionRewardHandler: CommissionRewardHandler,
    private val referenceHandler: ReferenceHandler
) {
    data class ReferrerReportBody(
        val referentsCount: Long,
        val share: BigDecimal
    )

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
    @GetMapping("/reports/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getReportByRewardedUuid(
        @PathVariable uuid: String,
        principal: Principal
    ): ReferrerReportBody {
        if (uuid != principal.name) throw OpexException(OpexError.UnAuthorized)
        val referencesCount = referenceHandler.findByReferrerUuid(uuid).size.toLong()
        val commissions = commissionRewardHandler.findCommissions(rewardedUuid = uuid)
        return ReferrerReportBody(referencesCount, commissions.sumOf { it.share })
    }
}
