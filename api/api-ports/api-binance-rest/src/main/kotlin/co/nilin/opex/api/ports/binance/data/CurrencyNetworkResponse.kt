package co.nilin.opex.api.ports.binance.data

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

data class CurrencyNetworkResponse(
    val currency: String,
    val name: String,
    val chains: List<CurrencyNetwork>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CurrencyNetwork(
    val network: String,
    val currency: String,
    val minWithdraw: BigDecimal,
    val withdrawFee: BigDecimal,
    val isToken: Boolean,
    val tokenAddress: String?
)