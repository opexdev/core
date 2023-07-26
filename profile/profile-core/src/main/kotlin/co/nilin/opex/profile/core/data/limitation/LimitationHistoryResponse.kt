package co.nilin.opex.profile.core.data.limitation

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class LimitationHistoryResponse(var response:Map<String?,List<LimitationHistory>>?=null, var totalData:List<LimitationHistory>?=null)
