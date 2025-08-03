package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Component
class AccountantProxyImpl(private val restTemplate: RestTemplate) : AccountantProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.accountant.url}")
    private lateinit var baseUrl: String

    override fun getPairConfigs(): List<PairConfigResponse> {
        logger.info("fetching pair configs")
        return restTemplate.exchange<Array<PairConfigResponse>>(
            "$baseUrl/config/all",
            HttpMethod.GET,
            noBody()
        ).body?.toList() ?: emptyList()
    }

    override fun getFeeConfigs(): List<PairFeeResponse> {
        logger.info("fetching fee configs")
        return restTemplate.exchange<Array<PairFeeResponse>>(
            "$baseUrl/config/fee",
            HttpMethod.GET,
            noBody()
        ).body?.toList() ?: emptyList()
    }

    override fun getFeeConfig(symbol: String): PairFeeResponse {
        logger.info("fetching fee configs for $symbol")
        return restTemplate.exchange<PairFeeResponse>(
            "$baseUrl/config/fee/$symbol",
            HttpMethod.GET,
            noBody()
        ).body!!
    }
}