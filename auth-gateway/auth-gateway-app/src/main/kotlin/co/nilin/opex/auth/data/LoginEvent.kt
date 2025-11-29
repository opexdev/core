package co.nilin.opex.auth.data

import org.keycloak.services.scheduled.ClearExpiredEvents
import java.time.LocalDateTime

data class LoginEvent(
    val uuid: String,
    val sessionId: String?,
    val deviceUuid: String?,
    val appVersion: String?,
    val osVersion: String?,
    val expireDate: LocalDateTime,
    val os: String?,
) : AuthEvent()