package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.limitation.*
import kotlinx.coroutines.flow.Flow

interface LimitationPersister {
    suspend fun updateLimitation(updatePermissionRequest: UpdateLimitationRequest)

    suspend fun getLimitation(userId: String?, action: ActionType? = null, reason: LimitationReason? = null, offset: Int? = 0, size: Int? = 1000): Flow<Limitation>?

    suspend fun getLimitationHistory(userId: String?, action: ActionType?, reason: LimitationReason?, offset: Int, size: Int): Flow<LimitationHistory>?


}