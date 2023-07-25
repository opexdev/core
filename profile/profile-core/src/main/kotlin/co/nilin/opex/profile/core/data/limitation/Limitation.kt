package co.nilin.opex.profile.core.data.limitation

import java.time.LocalDateTime

data class Limitation(
        var expTime:LocalDateTime?,
        var userId:String?,
        var actionType:ActionType?,
        var createData:LocalDateTime?,
        var detail:String?,
        var description:String?
)
