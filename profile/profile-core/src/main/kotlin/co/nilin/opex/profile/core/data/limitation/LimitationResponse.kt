package co.nilin.opex.profile.core.data.limitation

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class LimitationResponse(var response:Map<String?,List<Limitation>>?=null, var totalData:List<Limitation>?=null)