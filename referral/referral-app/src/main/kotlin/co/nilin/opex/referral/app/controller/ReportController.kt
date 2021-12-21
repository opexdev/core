package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import co.nilin.opex.referral.core.spi.ReferralCodeHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class ReportController(
    private val commissionRewardHandler: CommissionRewardHandler,
    private val referralCodeHandler: ReferralCodeHandler,
    private val referenceHandler: ReferenceHandler
) {
    data class ReportBody(
        val code: String,
        val referentCounts: Long,
        val tradeCount: Long,
        val referrerShare: BigDecimal,
        val referentShare: BigDecimal
    )

    @GetMapping("/reports/{code}")
    suspend fun getReportByCode(@PathVariable code: String): ReportBody {
        val references = referenceHandler.findByCode(code)
        val commissions = commissionRewardHandler.findCommissions(referralCode = code)
        return ReportBody(
            code,
            references.size.toLong(),
            commissions.size.toLong(),
            commissions.sumOf { it.referrerShare },
            commissions.sumOf { it.referentShare }
        )
    }

    @GetMapping("/reports")
    suspend fun getReportByReferrer(@RequestParam referrerUuid: String): List<ReportBody> {
        val references = referenceHandler.findByReferrerUuid(referrerUuid)
        val codes = referralCodeHandler.findByReferrerUuid(referrerUuid)
        val commissions = commissionRewardHandler.findCommissions(referrerUuid = referrerUuid)
        return codes.map {
            ReportBody(
                it.code,
                references.size.toLong(),
                commissions.size.toLong(),
                commissions.sumOf { c -> c.referrerShare },
                commissions.sumOf { c -> c.referentShare }
            )
        }
    }
}
