package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.core.data.limitation.Limitation
import co.nilin.opex.profile.core.data.limitation.UpdateLimitationRequest

interface LimitationPersister {
   suspend fun  updateLimitation(updatePermissionRequest: UpdateLimitationRequest)
   suspend fun  getLimitation(userId:String?,action: ActionType?):List<Limitation>?


}