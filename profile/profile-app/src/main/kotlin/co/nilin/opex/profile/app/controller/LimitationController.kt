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

    @GetMapping("")
    suspend fun getLimitation(@RequestParam("userId") userId: String?,
                              @RequestParam("action") action: ActionType?,
                              @RequestParam("reason") reason: LimitationReason?,
                              @RequestParam("groupBy") groupBy: String?,
                              @RequestParam("size") size: Int?,
                              @RequestParam("offset") offset: Int?): LimitationResponse? {

        var res = limitManagement.getLimitation(userId, action, reason, offset ?: 0, size ?: 1000)

        return when (groupBy) {
            "user" -> LimitationResponse(res?.groupBy { r -> r.userId })
            "action" -> LimitationResponse(res?.groupBy { r -> r.actionType?.name })
            "reason" -> LimitationResponse(res?.groupBy { r -> (r.reason ?: LimitationReason.Other).name })
            else -> {
                LimitationResponse(totalData = res)
            }
        }

    }

    @GetMapping("/history")
    suspend fun getLimitationHistory(@RequestParam("userId") userId: String?,
                                     @RequestParam("action") action: ActionType?,
                                     @RequestParam("reason") reason: LimitationReason?,
                                     @RequestParam("groupBy") groupBy: String?,
                                     @RequestParam("size") size: Int?,
                                     @RequestParam("offset") offset: Int?): LimitationHistoryResponse? {

        var res = limitManagement.getLimitationHistory(userId, action,reason, offset ?: 0, size ?: 1000)
        return when (groupBy) {
            "user" -> LimitationHistoryResponse(res?.groupBy { r -> r.userId })
            "action" -> LimitationHistoryResponse(res?.groupBy { r -> r.actionType?.name })
            "reason" -> LimitationHistoryResponse(res?.groupBy { r -> (r.reason ?: LimitationReason.Other).name })
            else -> {
                LimitationHistoryResponse(totalData = res)
            }
        }
    }
}