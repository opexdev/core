package co.nilin.opex.admin.ports.auth.controller

import co.nilin.opex.admin.ports.auth.data.*
import co.nilin.opex.admin.ports.auth.service.AuthAdminService
import co.nilin.opex.admin.ports.auth.utils.asKeycloakUser
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/v1")
class AuthAdminController(private val service: AuthAdminService) {

    @GetMapping("/user")
    suspend fun getAllKeycloakUsers(@RequestParam offset: Int, @RequestParam size: Int): QueryUserResponse {
        return service.findAllUsers(offset, size)
    }

    @GetMapping("/user/{userId}")
    suspend fun getUser(@PathVariable userId: String): KeycloakUser {
        return service.getUser(userId).asKeycloakUser(true).apply {
            groups = service.getUserGroups(userId).map { KeycloakGroup(it.id, it.name) }
        }
    }

    @PostMapping("/user/{userId}/join-kyc")
    fun switchKYCGroup(@PathVariable userId: String, @RequestParam kycGroup: KycGroup) {
        service.switchKYCGroup(userId, kycGroup)
    }

    @PostMapping("/user/{userId}/kyc/accept")
    fun acceptKYC(@PathVariable userId: String) {
        service.switchKYCGroup(userId, KycGroup.ACCEPTED)
    }

    @PostMapping("/user/impersonate", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun impersonate(@RequestBody body: ImpersonateRequest): String {
        return service.impersonate(body.clientId, body.clientSecret, body.userId)
    }

    @GetMapping("/user/search")
    suspend fun searchUsers(
        @RequestParam search: String,
        @RequestParam(required = false) by: String?,
        @RequestParam offset: Int,
        @RequestParam size: Int
    ): QueryUserResponse {
        return if (by == "email") service.searchUserEmail(search)
        else service.searchUser(search, offset, size)
    }

    @PostMapping("/user/{userId}/kyc/reject")
    fun rejectKYC(@PathVariable userId: String) {
        service.switchKYCGroup(userId, KycGroup.REJECTED)
    }

    @GetMapping("/group/{groupName}/members")
    fun getMembersOfGroup(
        @PathVariable groupName: String, @RequestParam offset: Int, @RequestParam size: Int
    ): QueryUserResponse {
        return service.findUsersInGroupByName(groupName, offset, size)
    }

}