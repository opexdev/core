package co.nilin.opex.profile.app.service

import co.nilin.opex.profile.core.data.limitation.*
import co.nilin.opex.profile.core.spi.LimitationPersister
import org.springframework.stereotype.Component

@Component
class LimitationManagement(private var limitationPersister: LimitationPersister) {
    suspend fun updateLimitation(permissionControlRequest: UpdateLimitationRequest) {
        limitationPersister.updateLimitation(permissionControlRequest)

    }

    suspend fun getLimitation(userId: String?, action: ActionType?,reason: LimitationReason?,offset:Int,size:Int): List<Limitation>? {
        return limitationPersister.getLimitation(userId, action,reason,offset,size)
    }

    suspend fun getLimitationHistory(userId: String?, action: ActionType?,reason:LimitationReason?,offset:Int,size:Int): List<LimitationHistory>? {
        return limitationPersister.getLimitationHistory(userId, action,reason,offset,size)
    }

}