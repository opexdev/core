package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.CurrencyImplementation
import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.AssignAddressRequest
import co.nilin.opex.api.ports.proxy.data.DepositDetailsRequest
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class BlockchainGatewayProxyImpl(private val restTemplate: RestTemplate) : BlockchainGatewayProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.opex-bc-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun assignAddress(uuid: String, currency: String, chain: String): AssignResponse? {
        logger.info("calling bc-gateway assign")
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/address/assign")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(AssignAddressRequest(uuid, currency, chain))

            restTemplate.exchange(
                request,
                AssignResponse::class.java
            ).body ?: throw RuntimeException("Failed to assign address")
        }
    }

    override suspend fun getDepositDetails(refs: List<String>): List<DepositDetails> {
        logger.info("calling bc-gateway deposit details")
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/deposit/find/all")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(DepositDetailsRequest(refs))

            restTemplate.exchange(
                request,
                Array<DepositDetails>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getCurrencyImplementations(currency: String?): List<CurrencyImplementation> {
        logger.info("calling bc-gateway chain details")
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/currency/chains")
                .queryParam("currency", currency)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<CurrencyImplementation>::class.java
            ).body?.toList() ?: emptyList()
        }
    }
}