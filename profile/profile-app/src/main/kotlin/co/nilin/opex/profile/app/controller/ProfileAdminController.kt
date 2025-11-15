package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.UpdateApprovalRequestBody
import co.nilin.opex.profile.app.service.ProfileApprovalRequestManagement
import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.limitation.*
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.ports.postgres.imp.LimitationManagementImp
import kotlinx.coroutines.flow.toList
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/profile")

class ProfileAdminController(
    val profileManagement: ProfileManagement,
    val profileApprovalRequestManagement: ProfileApprovalRequestManagement,
    val limitManagement: LimitationManagementImp
) {

    @PostMapping("")
    suspend fun getProfiles(@RequestBody profileRequest: ProfileRequest): List<Profile> {
        return profileManagement.getAllProfiles(profileRequest)
    }

    @GetMapping("/{userId}")
    suspend fun getProfile(@PathVariable("userId") userId: String): Profile {
        return profileManagement.getProfile(userId)
    }

    @GetMapping("/history/{userId}")
    suspend fun getProfileHistory(
        @PathVariable("userId") userId: String,
        @RequestParam offset: Int?, @RequestParam limit: Int?
    ): List<ProfileHistory>? {
        return profileManagement.getHistory(userId, offset ?: 0, limit ?: 10)
    }

    // =====================================Approval Requests====================================

    @PostMapping("/approval-requests")
    suspend fun getApprovalRequests(@RequestBody request: ProfileApprovalRequestFilter): List<ProfileApprovalAdminResponse> {
        return profileApprovalRequestManagement.getApprovalRequests(request)
    }

    @GetMapping("/approval-request/{id}")
    suspend fun getApprovalRequest(@PathVariable("id") id: Long): ProfileApprovalAdminResponse {
        return profileApprovalRequestManagement.getApprovalRequestById(id)
    }

    @PutMapping("/approval-request")
    suspend fun updateRequestStatus(
        @RequestBody changeRequestStatusBody: UpdateApprovalRequestBody,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileApprovalAdminResponse {
        return profileApprovalRequestManagement.changeRequestStatus(
            changeRequestStatusBody.id,
            securityContext.authentication.name,
            changeRequestStatusBody.description,
            changeRequestStatusBody.status
        )
    }

    //==============================================limitation services=================================================


    @PostMapping("/limitation")
    suspend fun updateLimitation(@RequestBody permissionRequest: UpdateLimitationRequest) {
        permissionRequest.reason ?: LimitationReason.Other
        limitManagement.updateLimitation(permissionRequest)
    }

    @GetMapping("/limitation")
    suspend fun getLimitation(
        @RequestParam("userId") userId: String?,
        @RequestParam("action") action: ActionType?,
        @RequestParam("reason") reason: LimitationReason?,
        @RequestParam("groupBy") groupBy: String?,
        @RequestParam("size") size: Int?,
        @RequestParam("offset") offset: Int?
    ): LimitationResponse? {

        var res = limitManagement.getLimitation(userId, action, reason, offset ?: 0, size ?: 1000)?.toList()

        return when (groupBy) {
            "user" -> LimitationResponse(res?.groupBy { r -> r.userId })
            "action" -> LimitationResponse(res?.groupBy { r -> r.actionType?.name })
            "reason" -> LimitationResponse(res?.groupBy { r -> (r.reason ?: LimitationReason.Other).name })
            else -> {
                LimitationResponse(totalData = res)
            }
        }

    }

    @GetMapping("/limitation/history")
    suspend fun getLimitationHistory(
        @RequestParam("userId") userId: String?,
        @RequestParam("action") action: ActionType?,
        @RequestParam("reason") reason: LimitationReason?,
        @RequestParam("groupBy") groupBy: String?,
        @RequestParam("size") size: Int?,
        @RequestParam("offset") offset: Int?
    ): LimitationHistoryResponse? {

        var res = limitManagement.getLimitationHistory(userId, action, reason, offset ?: 0, size ?: 1000)?.toList()
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