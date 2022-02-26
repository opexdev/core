package co.nilin.opex.admin.ports.auth.controller

import co.nilin.opex.admin.ports.auth.data.ImpersonateRequest
import co.nilin.opex.admin.ports.auth.data.KeycloakUser
import co.nilin.opex.admin.ports.auth.data.KycGroup
import co.nilin.opex.admin.ports.auth.service.AuthAdminService
import co.nilin.opex.admin.ports.auth.utils.asKeycloakUser
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/v1")
class AuthAdminController(private val service: AuthAdminService) {

    @GetMapping("/user")
    suspend fun getAllKeycloakUsers(): List<KeycloakUser> {
        return service.findAllUsers().map { it.asKeycloakUser() }
    }

    @PostMapping("/user/{userId}/join-kyc")
    fun switchKYCGroup(@PathVariable userId: String, @RequestParam kycGroup: KycGroup) {
        service.switchKYCGroup(userId, kycGroup)
    }

    @PostMapping("/user/{userId}/kyc/accept")
    fun acceptKYC(@PathVariable userId: String) {
        service.switchKYCGroup(userId, KycGroup.ACCEPTED)
    }

    @PostMapping("/user/{userId}/kyc/reject")
    fun rejectKYC(@PathVariable userId: String) {
        service.switchKYCGroup(userId, KycGroup.REJECTED)
    }

    @GetMapping("/group/{groupName}/members")
    fun getMembersOfGroup(@PathVariable groupName: String): List<KeycloakUser> {
        return service.findUsersInGroupByName(groupName).map { it.asKeycloakUser() }
    }

    @PostMapping("/user/impersonate", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun impersonate(@RequestBody body: ImpersonateRequest): String {
        return service.impersonate(body.clientId, body.clientSecret, body.userId)
    }

}