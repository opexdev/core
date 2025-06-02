package co.nilin.opex.api.ports.opex.data

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NewOrderResponse(
    val symbol: String,
    val date: Date = Date()
)