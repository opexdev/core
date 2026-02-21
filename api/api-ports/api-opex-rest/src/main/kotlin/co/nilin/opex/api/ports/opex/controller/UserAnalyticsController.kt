package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.analytics.ActivityTotals
import co.nilin.opex.api.ports.opex.service.UserActivityAggregationService
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import kotlin.random.Random

@RestController
@RequestMapping("/opex/v1/analytics")
class UserAnalyticsController(private val userActivityAggregationService: UserActivityAggregationService) {

    @GetMapping("/user-activity")
    suspend fun userActivity(@CurrentSecurityContext securityContext: SecurityContext): Map<Long, ActivityTotals> {
        val auth=securityContext.jwtAuthentication()
        return userActivityAggregationService.getLast31DaysUserStats(auth.tokenValue(),auth.name)
    }
}
