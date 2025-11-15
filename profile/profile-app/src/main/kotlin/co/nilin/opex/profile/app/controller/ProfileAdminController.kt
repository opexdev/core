package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.ProfileApprovalRequestManagement
import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.limitation.*
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.ports.postgres.imp.LimitationManagementImp
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
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

    data class ChangeRequestStatusBody(
        val id: Long,
        val description: String?
    )

    @PostMapping("/{userId}")
    suspend fun createManually(@PathVariable("userId") userId: String, @RequestBody newProfile: Profile): Profile? {
        return profileManagement.create(userId, newProfile)?.awaitFirstOrNull()
    }

    @PutMapping("/{userId}")
    suspend fun updateAsAdmin(@PathVariable("userId") userId: String, @RequestBody newProfile: Profile): Profile? {
        return profileManagement.updateAsAdmin(userId, newProfile)?.awaitFirstOrNull()
    }

    @GetMapping("/history/{userId}")
    suspend fun getHistory(
        @PathVariable("userId") userId: String,
        @RequestParam offset: Int?, @RequestParam limit: Int?
    ): List<ProfileHistory>? {
        return profileManagement.getHistory(userId, offset ?: 0, limit ?: 10)
    }

    @PostMapping("")
    suspend fun getProfiles(@RequestBody profileRequest: ProfileRequest): List<Profile> {
        return profileManagement.getAllProfiles(profileRequest)
    }

    @GetMapping("/{userId}")
    suspend fun getProfile(@PathVariable("userId") userId: String): Profile {
        return profileManagement.getProfile(userId)
    }

    // =====================================Approval Requests====================================

    @GetMapping("/approval-requests/{status}")
    suspend fun getApprovalRequests(@PathVariable("status") status: ProfileApprovalRequestStatus): List<ProfileApprovalAdminResponse> {
        return profileApprovalRequestManagement.getApprovalRequests(status)
    }

    @GetMapping("/approval-request/{id}")
    suspend fun getApprovalRequest(@PathVariable("id") id: Long): ProfileApprovalAdminResponse {
        return profileApprovalRequestManagement.getApprovalRequestById(id)
    }

    @PostMapping("/approve-request")
    suspend fun approveRequest(
        @RequestBody changeRequestStatusBody: ChangeRequestStatusBody,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileApprovalAdminResponse {
        return profileApprovalRequestManagement.approveRequest(
            changeRequestStatusBody.id,
            securityContext.authentication.name,
            changeRequestStatusBody.description
        )
    }

    @PostMapping("/reject-request")
    suspend fun rejectRequest(
        @RequestBody changeRequestStatusBody: ChangeRequestStatusBody,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileApprovalAdminResponse {
        return profileApprovalRequestManagement.rejectRequest(
            changeRequestStatusBody.id,
            securityContext.authentication.name,
            changeRequestStatusBody.description
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