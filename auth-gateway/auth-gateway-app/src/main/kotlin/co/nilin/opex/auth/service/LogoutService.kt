package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.LogoutEvent
import co.nilin.opex.auth.kafka.AuthEventProducer
import co.nilin.opex.auth.proxy.KeycloakProxy
import org.springframework.stereotype.Service

@Service
class LogoutService(
    private val keycloakProxy: KeycloakProxy,
    private val authEventProducer: AuthEventProducer,
) {

    suspend fun logout(userId: String, sessionId: String) {
        keycloakProxy.logoutSession(userId, sessionId)
        sendLogoutEvent(userId, sessionId)
    }

    suspend fun logoutSession(uuid: String, sessionId: String) {
        keycloakProxy.logoutSession(uuid, sessionId)
    }

    suspend fun logoutOthers(uuid: String, currentSessionId: String) {
        keycloakProxy.logoutOthers(uuid, currentSessionId)
        sendLogoutEvent(uuid, currentSessionId, true)
    }

    suspend fun logoutAll(uuid: String) {
        keycloakProxy.logoutAll(uuid)
    }


    private fun sendLogoutEvent(userId: String, sessionState: String?, others: Boolean? = false) {
        authEventProducer.send(
            LogoutEvent(
                userId,
                sessionState,
                others
            )
        )
    }
}