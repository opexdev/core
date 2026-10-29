package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.DepositDetailsRequest
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono
import java.net.URI

@Component
class BlockchainGatewayProxyImpl(@Qualifier("nlbWebClient") private val client: WebClient) :
    BlockchainGatewayProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.opex-bc-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun assignAddress(assignAddressRequest: AssignAddressRequest): AssignResponse? {
        logger.info("calling bc-gateway assign")
        return withContext(ProxyDispatchers.general) {
            client.post()
                .uri(URI.create("$baseUrl/v1/address/assign"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(assignAddressRequest))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(AssignResponse::class.java)
                .awaitSingleOrNull()
        }
    }

    override suspend fun getDepositDetails(refs: List<String>): List<DepositDetails> {
        logger.info("calling bc-gateway deposit details")
        return withContext(ProxyDispatchers.general) {
            client.post()
                .uri(URI.create("$baseUrl/deposit/find/all"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(DepositDetailsRequest(refs)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<DepositDetails>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getChainInfo(): List<ChainInfo> {
        logger.info("calling bc-gateway chains info")
        return withContext(ProxyDispatchers.general) {
            client.get()
                .uri(URI.create("$baseUrl/crypto-currency/chain"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<ChainInfo>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getOnChainGatewayLocalizations(
        token: String,
        gatewayUuid: String
    ): GatewayLocalizationResponse {
        return client.get()
            .uri("$baseUrl/crypto-currency/gateway/${gatewayUuid}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<GatewayLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun saveOnChainGatewayLocalizations(
        token: String,
        gatewayUuid: String,
        gatewayLocalizations: List<GatewayLocalizationCommand>
    ): GatewayLocalizationResponse {
        return client.post()
            .uri("$baseUrl/crypto-currency/gateway/${gatewayUuid}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(gatewayLocalizations))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<GatewayLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun searchAddressAssignments(
        token: String,
        filter: AddressAssignmentFilter
    ): AddressAssignmentPage {
        return client.post()
            .uri("$baseUrl/admin/address/assignments/search")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(filter))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<AddressAssignmentPage>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to search address assignments") }
    }

    override suspend fun getAddressAssignment(token: String, id: Long): AddressAssignmentRecord {
        return client.get()
            .uri("$baseUrl/admin/address/assignments/$id")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<AddressAssignmentRecord>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get address assignment $id") }
    }

    override suspend fun searchReservedAddresses(token: String, filter: ReservedAddressFilter): ReservedAddressPage {
        return client.post()
            .uri("$baseUrl/admin/address/reserved/search")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(filter))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<ReservedAddressPage>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to search reserved addresses") }
    }

    override suspend fun assignAddressByAdmin(
        token: String,
        request: AdminAssignAddressRequest
    ): AddressAssignmentRecord {
        return client.post()
            .uri("$baseUrl/admin/address/assign")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<AddressAssignmentRecord>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to assign address") }
    }

    override suspend fun revokeAddressAssignment(token: String, id: Long): AddressAssignmentRecord {
        return client.post()
            .uri("$baseUrl/admin/address/assignments/$id/revoke")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<AddressAssignmentRecord>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to revoke address assignment $id") }
    }

    override suspend fun deleteOnChainGatewayLocalization(token: String, id: Long) {
        client.delete()
            .uri("$baseUrl/crypto-currency/gateway/localization/${id}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
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