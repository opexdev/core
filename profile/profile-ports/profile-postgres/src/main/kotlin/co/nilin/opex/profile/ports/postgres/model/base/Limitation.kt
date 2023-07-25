package co.nilin.opex.profile.ports.postgres.model.base

import co.nilin.opex.profile.core.data.limitation.ActionType
import java.time.LocalDateTime
import java.util.*

open class Limitation {
    lateinit var userId: String;
    var actionType: ActionType?=null;
    var createDate: LocalDateTime?=null;
    var expTime:Long?=null;
    var detail:String?=null
    var limitationOn:String?=null
    var description:String?=null
}