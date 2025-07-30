package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Component
class AccountantProxyImpl(private val restTemplate: RestTemplate) : AccountantProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.accountant.url}")
    private lateinit var baseUrl: String

    override suspend fun getPairConfigs(): List<PairConfigResponse> {
        logger.info("fetching pair configs")
        return withContext(Dispatchers.IO) {
            restTemplate.getForObject<Array<PairConfigResponse>>("$baseUrl/config/all", defaultHeaders()).toList()
        }
    }

    override suspend fun getFeeConfigs(): List<PairFeeResponse> {
        logger.info("fetching fee configs")
        return withContext(Dispatchers.IO) {
            restTemplate.getForObject<Array<PairFeeResponse>>("$baseUrl/config/fee", defaultHeaders()).toList()
        }
    }

    override suspend fun getFeeConfig(symbol: String): PairFeeResponse {
        logger.info("fetching fee configs for $symbol")
        return withContext(Dispatchers.IO) {
            restTemplate.getForObject<PairFeeResponse>("$baseUrl/config/fee/$symbol", defaultHeaders())
        }
    }
}