package co.nilin.opex.api.ports.binance.data

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.RequestParam

data class WithDrawRequest(
        var coin: String?,
        var withdrawOrderId: String?,
        @JsonProperty("status")
        var withdrawStatus: Int?,
        var offset: Int?,
        var limit: Int?,
        var startTime: Long?,
        var endTime: Long?,
        var ascendingByTime: Boolean? = false,
        var recvWindow: Long?, //The value cannot be greater than 60000
        var timestamp: Long,
)
