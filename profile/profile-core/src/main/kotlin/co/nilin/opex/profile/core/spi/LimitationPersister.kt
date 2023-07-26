package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.limitation.*

interface LimitationPersister {
    suspend fun updateLimitation(updatePermissionRequest: UpdateLimitationRequest)

    suspend fun getLimitation(userId: String?, action: ActionType?,reason:LimitationReason?,offset: Int, size: Int): List<Limitation>?

    suspend fun getLimitationHistory(userId: String?, action: ActionType?,reason:LimitationReason?,offset: Int, size: Int): List<LimitationHistory>?


}