package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.LimitationManagement
import co.nilin.opex.profile.core.data.limitation.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/profile/limitation")

class LimitationController(private var limitManagement: LimitationManagement) {
    @PostMapping("")
    suspend fun updateLimitation(@RequestBody permissionRequest: UpdateLimitationRequest) {
        limitManagement.updateLimitation(permissionRequest)
    }

    @GetMapping("/{userId}/{action}")
    suspend fun getLimitationPerUserAction(@PathVariable("userId") userId: String,
                                           @PathVariable("action") action: ActionType):LimitationPerUserActionResponse {
      return LimitationPerUserActionResponse(limitManagement.getLimitation(userId, action))
    }

    @GetMapping("/{userId}")
    suspend fun getLimitationPerUser(@PathVariable("userId") userId: String): LimitationPerUserResponse {
        return LimitationPerUserResponse(limitManagement.getLimitation(userId, null)?.groupBy { g -> g.userId!! })
    }

    @GetMapping("/{action}")
    suspend fun getLimitationPerAction(
            @PathVariable("action") action: ActionType): LimitationPerActionResponse {
        return LimitationPerActionResponse(limitManagement.getLimitation(null, action)?.groupBy { g -> g.actionType!!.name })
    }

    @GetMapping("")
    suspend fun getLimitations() {
        limitManagement.getLimitation(null, null)
    }


}