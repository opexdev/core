package co.nilin.opex.admin.ports.auth.service

import co.nilin.opex.admin.ports.auth.data.KycGroup
import co.nilin.opex.admin.ports.auth.data.QueryUserResponse
import co.nilin.opex.admin.ports.auth.proxy.KeycloakProxy
import co.nilin.opex.admin.ports.auth.utils.asKeycloakUser
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.GroupResource
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.GroupRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service

@Service
class AuthAdminService(
    private val keycloak: Keycloak,
    private val opexRealm: RealmResource,
    private val proxy: KeycloakProxy
) {

    fun getUser(userId: String): UserRepresentation {
        return opexRealm.users().get(userId).toRepresentation() ?: throw OpexException(OpexError.UserNotFoundAdmin)
    }

    fun getUserGroups(userId: String): List<GroupRepresentation> {
        return opexRealm.users().get(userId).groups()
    }

    fun findAllUsers(offset: Int, size: Int): QueryUserResponse {
        return QueryUserResponse(
            opexRealm.users().count(),
            opexRealm.users().list(offset, size).map { it.asKeycloakUser() }
        )
    }

    fun findGroupById(groupId: String): GroupResource {
        return opexRealm.groups().group(groupId) ?: throw OpexException(OpexError.NotFound, "Group not found")
    }

    fun findGroupByName(groupName: String): GroupResource {
        val groupRep = opexRealm.groups()
            .groups()
            .find { it.name == groupName }
            ?: throw OpexException(OpexError.NotFound, "Group not found")

        return opexRealm.groups().group(groupRep.id)
    }

    fun findUsersInGroupById(groupId: String): List<UserRepresentation> {
        val group = findGroupById(groupId)
        return group.members()
    }

    fun findUsersInGroupByName(groupName: String, offset: Int, size: Int): QueryUserResponse {
        val group = findGroupByName(groupName)
        val members = group.members(offset, size)
        return QueryUserResponse(
            members.count(),
            members.map { it.asKeycloakUser() }
        )
    }

    fun addUserToGroup(userId: String, groupId: String) {
        val user = opexRealm.users().get(userId) ?: throw OpexException(OpexError.NotFound, "User not found")
        user.joinGroup(groupId)
    }

    fun removeUserFromGroup(userId: String, groupId: String) {
        val user = opexRealm.users().get(userId) ?: throw OpexException(OpexError.NotFound, "User not found")
        user.leaveGroup(groupId)
    }

    fun rejectKYC(userId: String, reason: String) {
        switchKYCGroup(userId, KycGroup.REJECTED)
        val user = opexRealm.users().get(userId)
        with(user.toRepresentation()) {
            attributes[".rejectReason"] = mutableListOf(reason)
            user.update(this)
        }
    }

    fun blockKYC(userId: String, reason: String) {
        switchKYCGroup(userId, KycGroup.BLOCKED)
        val user = opexRealm.users().get(userId)
        with(user.toRepresentation()) {
            attributes[".blockReason"] = mutableListOf(reason)
            user.update(this)
        }
    }

    fun switchKYCGroup(userId: String, kycGroup: KycGroup) {
        val group = findGroupByName(kycGroup.groupName)
        val user = opexRealm.users().get(userId) ?: throw OpexException(OpexError.NotFound, "User not found")
        with(user) {
            groups().forEach { leaveGroup(it.id) }
            joinGroup(group.toRepresentation().id)
        }
    }

    suspend fun impersonate(clientId: String, clientSecret: String, userId: String): String {
        opexRealm.users().get(userId) ?: throw OpexException(OpexError.NotFound, "User not found")
        val token = keycloak.tokenManager().accessToken.token
        return proxy.impersonate(token, clientId, clientSecret, userId)
    }

    fun searchUser(search: String, offset: Int, size: Int): QueryUserResponse {
        return QueryUserResponse(
            opexRealm.users().search(search).count(),
            opexRealm.users().search(search, offset, size, false).map { it.asKeycloakUser() }
        )
    }

    fun searchUserEmail(search: String): QueryUserResponse {
        val users = opexRealm.users().search(search)
        return QueryUserResponse(
            users.count(),
            users.map { it.asKeycloakUser() }
        )
    }

}