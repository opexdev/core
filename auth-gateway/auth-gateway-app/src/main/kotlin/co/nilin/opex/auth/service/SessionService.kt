package co.nilin.opex.auth.service

import co.nilin.opex.auth.data.SessionRequest
import co.nilin.opex.auth.data.Sessions
import co.nilin.opex.auth.proxy.DeviceManagementProxy
import org.springframework.stereotype.Service

@Service
class SessionService (private val deviceManagementProxy: DeviceManagementProxy){
    suspend fun fetchSessions(sessionRequest: SessionRequest, currentSessionId: String): List<Sessions> {
        return deviceManagementProxy.getLastSessions(sessionRequest).stream()
            .map { if (it.sessionState == currentSessionId) it.apply { isCurrentSession = true } else it }.toList()
    }
}