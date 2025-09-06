package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.FeeConfig
import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.common.OpexError
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

    override fun getFeeConfigs(): List<FeeConfig> {
        logger.info("fetching fee configs")
        return restTemplate.exchange<Array<FeeConfig>>(
            "$baseUrl/fee/config",
            HttpMethod.GET,
            noBody()
        ).body?.toList() ?: emptyList()
    }

    override fun getUserFee(uuid: String): UserFee {
        logger.info("fetching user fee")
        return restTemplate.exchange<UserFee>(
            "$baseUrl/fee/${uuid}",
            HttpMethod.GET,
            noBody()
        ).body ?: throw OpexError.FeeConfigNotFound.exception()
    }
}