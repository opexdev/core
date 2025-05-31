package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.LimitationManagement
import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.core.data.limitation.LimitationReason
import co.nilin.opex.profile.core.data.limitation.LimitationResponse
import kotlinx.coroutines.flow.toList
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/profile/limitation")
class LimitationController(private var limitManagement: LimitationManagement) {

    @GetMapping("")
    suspend fun getLimitation(
        @RequestParam("action") action: ActionType?,
        @RequestParam("reason") reason: LimitationReason?,
        @CurrentSecurityContext securityContext: SecurityContext
    ): LimitationResponse? {
        return LimitationResponse(
            totalData = limitManagement.getLimitation(
                securityContext.authentication.name,
                action,
                reason,
                0,
                1000
            )?.toList()
        )
    }
}