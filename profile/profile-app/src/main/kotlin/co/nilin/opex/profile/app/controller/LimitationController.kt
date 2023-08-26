package co.nilin.opex.profile.app.controller

import co.nilin.opex.kyc.core.utils.convert
import co.nilin.opex.profile.app.service.LimitationManagement
import co.nilin.opex.profile.core.data.limitation.*
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/profile/limitation")

class LimitationController(private var limitManagement: LimitationManagement) {
    @GetMapping("")
    suspend fun getLimitation(@RequestParam("action") action: ActionType?,
                              @RequestParam("reason") reason: LimitationReason?,
                              @CurrentSecurityContext securityContext: SecurityContext): LimitationResponse? {

        return LimitationResponse(totalData = limitManagement.getLimitation(securityContext.authentication.name, action, reason, 0, 1000))


    }
}