package co.nilin.opex.profile.core.data.limitation

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Limitation(
        var expTime:LocalDateTime?,
        var userId:String?,
        var actionType:ActionType?,
        var createDate:LocalDateTime?,
        var detail:String?,
        var description:String?,
        var reason: LimitationReason?
)
