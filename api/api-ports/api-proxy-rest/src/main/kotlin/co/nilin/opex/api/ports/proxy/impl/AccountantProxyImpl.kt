package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.FeeConfig
import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.api.core.inout.WithdrawLimitConfig
import co.nilin.opex.api.core.inout.analytics.DailyAmount
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal

@Component
class AccountantProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : AccountantProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.accountant.url}")
    private lateinit var baseUrl: String

    override suspend fun getPairConfigs(): List<PairConfigResponse> {
        logger.info("fetching pair configs")
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri("$baseUrl/config/all")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<PairConfigResponse>()
                .collectList()
                .awaitSingle()
        }
    }

    override suspend fun getFeeConfigs(): List<FeeConfig> {
        logger.info("fetching fee configs")
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri("$baseUrl/config/fee")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<FeeConfig>()
                .collectList()
                .awaitSingle()
        }
    }


    override suspend fun getWithdrawLimitConfigs(): List<WithdrawLimitConfig> {
        logger.info("fetching withdraw limit configs")
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri("$baseUrl/config/withdraw-limit")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToFlux<WithdrawLimitConfig>()
                .collectList()
                .awaitSingle()
        }
    }

    override suspend fun getTotalWithdrawVolumeValue(uuid: String, interval: Interval?): BigDecimal {
        logger.info("fetching total withdraw volume value")
        val uriBuilder = UriComponentsBuilder.fromUriString("$baseUrl/user/data/withdraw/volume/total/$uuid")
        interval?.let { uriBuilder.queryParam("interval", it.toString()) }
        val uri = uriBuilder.build().toUri()

        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToMono<BigDecimal>()
                .awaitSingle()
        }
    }

    override suspend fun getUserFee(uuid: String): UserFee {
        logger.info("fetching user fee")
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri("$baseUrl/user/data/fee/$uuid")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToMono<UserFee>()
                .awaitSingle()
        }
    }

    override suspend fun getTradeVolumeByCurrency(uuid: String, symbol: String, interval: Interval): BigDecimal {
        logger.info("fetching total trade volume by currency")
        val uriBuilder = UriComponentsBuilder.fromUriString("$baseUrl/user/data/trade/volume/$uuid")
        interval?.let {
            uriBuilder.queryParam("interval", it.toString())
            uriBuilder.queryParam("symbol", symbol)
        }
        val uri = uriBuilder.build().toUri()

        return withContext(ProxyDispatchers.general) {
            webClient.get()
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
        val uriBuilder = UriComponentsBuilder.fromUriString("$baseUrl/user/data/trade/volume/total/$uuid")
        interval?.let { uriBuilder.queryParam("interval", it.toString()) }
        val uri = uriBuilder.build().toUri()

        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }, { it.createException() })
                .bodyToMono<BigDecimal>()
                .awaitSingle()
        }
    }

    override suspend fun getDailyWithdrawLast31Days(
        uuid: String): List<DailyAmount> {

        logger.info("fetching daily withdraw stats for {}", uuid)
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri("$baseUrl/user-activity/withdraw/$uuid")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { it.createException() }
                .bodyToMono<List<DailyAmount>>()
                .awaitSingle()
        }
    }

    override suspend fun getDailyDepositLast31Days(
        uuid: String): List<DailyAmount> {

        logger.info("fetching daily deposit stats for {}", uuid)

        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri("$baseUrl/user-activity/deposit/$uuid")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { it.createException() }
                .bodyToMono<List<DailyAmount>>()
                .awaitSingle()
        }
    }

    override suspend fun getDailyTradeLast31Days(
        uuid: String): List<DailyAmount> {

        logger.info("fetching daily trade stats for {}", uuid)

        return withContext(ProxyDispatchers.general) {
            webClient.get()
                .uri("$baseUrl/user-activity/trade/$uuid")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { it.createException() }
                .bodyToMono<List<DailyAmount>>()
                .awaitSingle()
        }
    }
}