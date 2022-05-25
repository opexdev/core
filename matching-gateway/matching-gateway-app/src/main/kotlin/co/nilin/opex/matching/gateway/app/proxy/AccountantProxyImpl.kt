package co.nilin.opex.matching.gateway.app.proxy

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.inout.BooleanResponse
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.math.BigDecimal

@Component
class AccountantProxyImpl(
    @Value("\${app.accountant.url}")
    val accountantBaseUrl: String,
    val webClient: WebClient
) : AccountantApiProxy {

    override suspend fun canCreateOrder(uuid: String, symbol: String, value: BigDecimal): Boolean {
        return webClient.get()
            .uri("$accountantBaseUrl/$uuid/create_order/${value}_${symbol}/allowed")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<BooleanResponse>()
            .log()
            .awaitFirst()
            .result
    }

    override suspend fun fetchPairConfig(pair: String, direction: OrderDirection): PairConfig {
        return webClient.get()
            .uri("$accountantBaseUrl/config/${pair}/${direction}")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<PairConfig>()
            .log()
            .awaitFirst()
    }
}