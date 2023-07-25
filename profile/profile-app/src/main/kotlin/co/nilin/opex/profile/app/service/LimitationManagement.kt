package co.nilin.opex.profile.app.service

import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.core.data.limitation.UpdateLimitationRequest
import co.nilin.opex.profile.core.spi.LimitationPersister
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.limitation.Limitation
@Component
class LimitationManagement(private var limitationPersister: LimitationPersister) {
    suspend fun updateLimitation(permissionControlRequest: UpdateLimitationRequest) {
        limitationPersister.updateLimitation(permissionControlRequest)


    }

    suspend fun getLimitation(userId: String?, action: ActionType?): List<Limitation>? {
        return limitationPersister.getLimitation(userId, action)
    }
}