package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.inout.PairInfoResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class AccountantProxyImpl(private val restTemplate: RestTemplate) : AccountantProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.accountant.url}")
    private lateinit var baseUrl: String

    override suspend fun getPairConfigs(): List<PairInfoResponse> {
        logger.info("fetching pair configs")
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/config/all")
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<PairInfoResponse>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getFeeConfigs(): List<PairFeeResponse> {
        logger.info("fetching fee configs")
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/config/fee")
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<PairFeeResponse>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getFeeConfig(symbol: String): PairFeeResponse {
        logger.info("fetching fee configs for $symbol")
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/config/fee/$symbol")
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                PairFeeResponse::class.java
            ).body ?: throw RuntimeException("Failed to get fee config for $symbol")
        }
    }
}