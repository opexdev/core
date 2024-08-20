package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandler
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.NewCurrencyImplementationRepository
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import co.nilin.opex.bcgateway.ports.postgres.util.toDto
import co.nilin.opex.bcgateway.ports.postgres.util.toModel
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@Component
class CurrencyHandlerImplV2(
        private val chainRepository: ChainRepository,
        private val currencyImplementationRepository: NewCurrencyImplementationRepository
) : CryptoCurrencyHandlerV2 {

    private val logger = LoggerFactory.getLogger(CryptoCurrencyHandler::class.java)

    override suspend fun createOnChainGateway(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        chainRepository.findByName(request.chain)
                ?.awaitFirstOrElse { throw OpexError.ChainNotFound.exception() }
        currencyImplementationRepository.findImpls(currencySymbol = request.currencySymbol, chain = request.chain, implementationSymbol = request.implementationSymbol)?.awaitFirstOrNull()?.let { throw OpexError.CurrencyIsExist.exception() }
        return doSave(request.toModel())?.toDto();
    }

    override suspend fun updateOnChainGateway(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        return loadImpls(FetchGateways(gatewayUuid = request.gatewayUuid, currencySymbol = request.currencySymbol))
                ?.awaitFirstOrElse { throw OpexError.ImplNotFound.exception() }?.let {
                    doSave(it.toDto().updateTo(request).toModel().apply { id = it.id })?.toDto()
                }
    }

    override suspend fun deleteOnChainGateway(implUuid: String, currency: String): Void? {

        loadImpls(FetchGateways(gatewayUuid = implUuid, currencySymbol = currency))
                ?.awaitFirstOrElse { throw OpexError.ImplNotFound.exception() }?.let {
                    try {
                        return currencyImplementationRepository.deleteByImplUuid(implUuid)?.awaitFirstOrNull()
                    } catch (e: Exception) {
                        throw OpexError.BadRequest.exception()

                    }
                }
        return null
    }

    override suspend fun fetchCurrencyOnChainGateways(data: FetchGateways?): CurrencyImps? {
        logger.info("going to fetch impls of ${data?.currencySymbol ?: "all currencies"}")
        return CurrencyImps(loadImpls(data)?.map { it.toDto() }
                ?.collect(Collectors.toList())?.awaitFirstOrNull())
    }

    override suspend fun fetchOnChainGateway(implUuid: String, currency: String): CryptoCurrencyCommand? {
        return loadImpls(FetchGateways(currencySymbol = currency, gatewayUuid = implUuid))?.awaitFirstOrNull()?.toDto()
    }

    private suspend fun loadImpls(request: FetchGateways?): Flux<CurrencyImplementationModel>? {
        return currencyImplementationRepository.findImpls(request?.currencySymbol, request?.gatewayUuid, request?.chain, request?.currencyImplementationName)
                ?: throw OpexError.ImplNotFound.exception()
    }

    private suspend fun loadImpl(request: String): Mono<CurrencyImplementationModel>? {
        return currencyImplementationRepository.findByImplUuid(request)
                ?: throw OpexError.ImplNotFound.exception()
    }

    private suspend fun doSave(request: CurrencyImplementationModel): CurrencyImplementationModel? {
        return currencyImplementationRepository.save(request).awaitSingleOrNull()
    }
}
