package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.math.BigDecimal

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

     override suspend fun getFeeConfigs(): List<FeeConfig> {
        logger.info("fetching fee configs")
        return withContext(Dispatchers.IO) {
            restTemplate.getForObject<Array<PairFeeResponse>>("$baseUrl/config/fee", defaultHeaders()).toList()
        }
    }
    override suspend fun getUserFee(uuid: String): UserFee {
        logger.info("fetching user fee")
        return restTemplate.exchange<UserFee>(
            "$baseUrl/user/data/fee/$uuid",
            HttpMethod.GET,
            noBody()
        ).body ?: throw OpexError.FeeConfigNotFound.exception()
    }

    override suspend fun getTradeVolumeByCurrency(uuid: String, symbol: String, interval: Interval): BigDecimal {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/user/data/trade/volume/$uuid")
            .queryParam("symbol", symbol)
            .queryParam("interval", interval.toString())
            .build().toUri()
        return restTemplate.exchange<BigDecimal>(uri, HttpMethod.GET, noBody()).body!!
    }

    override suspend fun getTotalTradeVolumeValue(uuid: String, interval: Interval): BigDecimal {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/user/data/trade/volume/total/$uuid")
            .queryParam("interval", interval.toString())
            .build().toUri()
        return restTemplate.exchange<BigDecimal>(uri, HttpMethod.GET, noBody()).body!!
    }

    override suspend fun getWithdrawLimitConfigs(): List<WithdrawLimitConfig> {
        logger.info("fetching withdraw limit configs")
        return restTemplate.exchange<Array<WithdrawLimitConfig>>(
            "$baseUrl/config/withdraw-limit",
            HttpMethod.GET,
            noBody()
        ).body?.toList() ?: emptyList()
    }

    override fun getTotalWithdrawVolumeValue(
        uuid: String,
        interval: Interval?
    ): BigDecimal {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/user/data/withdraw/volume/total/$uuid")
            .apply { interval?.let { queryParam("interval", it.toString()) } }
            .build()
            .toUri()
        return restTemplate.exchange<BigDecimal>(uri, HttpMethod.GET, noBody()).body!!
    }
}