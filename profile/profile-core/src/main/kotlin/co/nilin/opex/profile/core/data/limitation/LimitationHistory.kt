package co.nilin.opex.profile.core.data.limitation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class LimitationHistory(var expTime: LocalDateTime?,
                             var userId:String?,
                             var actionType:ActionType?,
                             var createDate: LocalDateTime?,
                             var detail:String?,
                             var description:String?,
                             var issuer: String?,
                             var changeRequestDate: LocalDateTime?,
                             var changeRequestType: String?,
                             var reason: LimitationReason?
        )
