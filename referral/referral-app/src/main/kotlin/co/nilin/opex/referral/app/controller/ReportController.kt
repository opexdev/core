package co.nilin.opex.referral.app.controller

import co.nilin.opex.referral.core.spi.CommissionRewardHandler
import co.nilin.opex.referral.core.spi.ReferenceHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class ReportController(
    private val commissionRewardHandler: CommissionRewardHandler,
    private val referenceHandler: ReferenceHandler
) {
    data class ReportBody(
        val code: String,
        val referentsCount: Long,
        val referrerShare: BigDecimal
    )

    @GetMapping("/reports/{code}")
    suspend fun getReportByCode(@PathVariable code: String): ReportBody {
        val referencesCount = referenceHandler.findByCode(code).size.toLong()
        val commissions = commissionRewardHandler.findCommissions(referralCode = code)
        return ReportBody(code, referencesCount, commissions.sumOf { it.share })
    }
}
