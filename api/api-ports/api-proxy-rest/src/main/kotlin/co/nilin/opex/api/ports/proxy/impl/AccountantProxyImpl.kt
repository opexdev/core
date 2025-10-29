package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@Component
class AccountantProxyImpl(private val client: WebClient) : AccountantProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.accountant.url}")
    private lateinit var baseUrl: String

    override suspend fun getPairConfigs(): List<PairConfigResponse> {
        logger.info("fetching pair configs")
        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri("$baseUrl/config/all")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToFlux<PairConfigResponse>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getFeeConfigs(): List<FeeConfig> {
        logger.info("fetching fee configs")
        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri("$baseUrl/config/fee")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToFlux<FeeConfig>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getUserFee(uuid: String): UserFee {
        logger.info("fetching user fee")
        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri("$baseUrl/user/data/fee/$uuid")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToMono<UserFee>()
                .awaitSingleOrNull() ?: throw OpexError.FeeConfigNotFound.exception()
        }
    }

    override suspend fun getTradeVolumeByCurrency(uuid: String, symbol: String, interval: Interval): BigDecimal {
        logger.info("fetching trade volume by currency")
        val url = UriComponentsBuilder.fromUriString("$baseUrl/user/data/trade/volume/$uuid")
            .queryParam("symbol", symbol)
            .queryParam("interval", interval.toString())
            .build().toUri()

        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToMono<BigDecimal>()
                .awaitSingle()
        }
    }

    override suspend fun getTotalTradeVolumeValue(uuid: String, interval: Interval): BigDecimal {
        logger.info("fetching total trade volume value")
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/user/data/trade/volume/total/$uuid")
            .queryParam("interval", interval.toString())
            .build().toUri()

        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToMono<BigDecimal>()
                .awaitSingle()
        }
    }

    override suspend fun getWithdrawLimitConfigs(): List<WithdrawLimitConfig> {
        logger.info("fetching withdraw limit configs")
        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri("$baseUrl/config/withdraw-limit")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToFlux<WithdrawLimitConfig>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getTotalWithdrawVolumeValue(uuid: String, interval: Interval?): BigDecimal {
        logger.info("fetching total withdraw volume value")
        val uriBuilder = UriComponentsBuilder.fromUriString("$baseUrl/user/data/withdraw/volume/total/$uuid")
        interval?.let { uriBuilder.queryParam("interval", it.toString()) }
        val uri = uriBuilder.build().toUri()

        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToMono<BigDecimal>()
                .awaitSingle()
        }
    }

}