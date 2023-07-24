package co.nilin.opex.profile.core.data.permission

data class UpdatePermissionRequest(var userId: String,
                                   var actions: List<ActionType>?,
                                   var exprTime: Long?,
                                   var type: PermissionType? = null,
                                   var description: String
)


enum class PermissionType{Revoke, Access}