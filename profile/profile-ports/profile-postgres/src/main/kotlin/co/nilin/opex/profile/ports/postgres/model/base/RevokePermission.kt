package co.nilin.opex.profile.ports.postgres.model.base

import co.nilin.opex.profile.core.data.permission.ActionType
import java.util.*

open class RevokePermission {
    lateinit var userId: String;
    var actionType: ActionType?=null;
    var createDate: Date?=null;
    var expTime:Long?=null;
    var detail:String?=null
}