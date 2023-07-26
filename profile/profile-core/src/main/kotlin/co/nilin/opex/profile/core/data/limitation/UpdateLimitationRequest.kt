package co.nilin.opex.profile.core.data.limitation

data class UpdateLimitationRequest(var userId: String?,
                                   var actions: List<ActionType>?,
                                   var exprTime: Long?,
                                   var updateType: LimitationUpdateType,
                                   var description: String?,
                                   var detail: String?,
                                   var reason: LimitationReason?
)


