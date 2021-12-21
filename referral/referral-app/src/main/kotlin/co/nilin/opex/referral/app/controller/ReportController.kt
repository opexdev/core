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
        val referrerShare: BigDecimal
    )

    @GetMapping("/reports/{code}")
    suspend fun getReportByCode(@PathVariable code: String): ReportBody {
        val referencesCount = referenceHandler.findByCode(code).size.toLong()
        val commissions = commissionRewardHandler.findCommissions(referralCode = code)
        return ReportBody(code, referencesCount, commissions.size.toLong(), commissions.sumOf { it.referrerShare })
    }

    @GetMapping("/reports")
    suspend fun getReportByReferrer(@RequestParam referrerUuid: String): List<ReportBody> {
        val referencesCount = referenceHandler.findByReferrerUuid(referrerUuid).size.toLong()
        val codes = referralCodeHandler.findByReferrerUuid(referrerUuid)
        val commissions = commissionRewardHandler.findCommissions(referrerUuid = referrerUuid)
        return codes.map {
            ReportBody(it.code, referencesCount, commissions.size.toLong(), commissions.sumOf { c -> c.referrerShare })
        }
    }
}
