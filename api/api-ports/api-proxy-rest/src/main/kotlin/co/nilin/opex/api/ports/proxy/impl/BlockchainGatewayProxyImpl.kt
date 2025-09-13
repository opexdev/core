package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.AssignAddressRequest
import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.ports.proxy.data.DepositDetailsRequest
import co.nilin.opex.api.ports.proxy.utils.body
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Component
class BlockchainGatewayProxyImpl(private val restTemplate: RestTemplate) : BlockchainGatewayProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.opex-bc-gateway.url}")
    private lateinit var baseUrl: String

    override fun assignAddress(assignAddressRequest: AssignAddressRequest): AssignResponse? {
        logger.info("calling bc-gateway assign")
        return restTemplate.exchange<AssignResponse>(
            "$baseUrl/v1/address/assign",
            HttpMethod.POST,
            body(assignAddressRequest)
        ).body
    }

    override fun getDepositDetails(refs: List<String>): List<DepositDetails> {
        logger.info("calling bc-gateway deposit details")
        return restTemplate.exchange<Array<DepositDetails>>(
            "$baseUrl/deposit/find/all",
            HttpMethod.POST,
            body(DepositDetailsRequest(refs))
        ).body?.toList() ?: emptyList()
    }

//    override suspend fun getCurrencyImplementations(currency: String?): List<CurrencyImplementation> {
//        logger.info("calling bc-gateway chain details")
//        return client.get()
//            .uri("$baseUrl/currency/chains") {
//                it.queryParam("currency", currency)
//                it.build()
//            }.accept(MediaType.APPLICATION_JSON)
//            .retrieve()
//            .onStatus({ t -> t.isError }, { it.createException() })
//            .bodyToFlux<CurrencyImplementation>()
//            .collectList()
//            .awaitFirstOrElse { emptyList() }
//    }
}