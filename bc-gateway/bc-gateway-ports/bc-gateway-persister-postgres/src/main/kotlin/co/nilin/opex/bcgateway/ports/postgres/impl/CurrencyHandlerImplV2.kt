package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.FetchGateways
import co.nilin.opex.bcgateway.core.model.WithdrawData
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.CurrencyImplementationRepository
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayModel
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
    private val currencyImplementationRepository: CurrencyImplementationRepository
) : CryptoCurrencyHandlerV2 {

    private val logger = LoggerFactory.getLogger(CurrencyHandlerImplV2::class.java)

    override suspend fun createOnChainGateway(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        chainRepository.findByName(request.chain)
            ?.awaitFirstOrElse { throw OpexError.ChainNotFound.exception() }
        currencyImplementationRepository.findGateways(
            currencySymbol = request.currencySymbol,
            chain = request.chain,
            implementationSymbol = request.implementationSymbol
        )
            ?.awaitFirstOrNull()?.let { throw OpexError.GatewayIsExist.exception() }
        return doSave(request.toModel())?.toDto();
    }

    override suspend fun updateOnChainGateway(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        return loadImpls(FetchGateways(gatewayUuid = request.gatewayUuid, currencySymbol = request.currencySymbol))
            ?.awaitFirstOrElse { throw OpexError.GatewayNotFount.exception() }?.let { oldGateway ->
                doSave(oldGateway.toDto().updateTo(request).toModel().apply { id = oldGateway.id })?.toDto()
            }
    }

    override suspend fun deleteOnChainGateway(gatewayUuid: String, currency: String): Void? {

        loadImpls(FetchGateways(gatewayUuid = gatewayUuid, currencySymbol = currency))
            ?.awaitFirstOrElse { throw OpexError.GatewayNotFount.exception() }?.let {
                try {
                    return currencyImplementationRepository.deleteByGatewayUuid(gatewayUuid)?.awaitFirstOrNull()
                } catch (e: Exception) {
                    throw OpexError.BadRequest.exception()

                }
            }
        return null
    }

    override suspend fun fetchCurrencyOnChainGateways(data: FetchGateways?): List<CryptoCurrencyCommand>? {
        logger.info("going to fetch impls of ${data?.currencySymbol ?: "all currencies"}")
        return loadImpls(data)?.map { it.toDto() }
            ?.collect(Collectors.toList())?.awaitFirstOrNull()
    }

    override suspend fun fetchOnChainGateway(gatewayUuid: String, symbol: String): CryptoCurrencyCommand? {
        return loadImpl(gatewayUuid, symbol)?.awaitFirstOrNull()?.toDto()
    }

    private suspend fun loadImpls(request: FetchGateways?): Flux<CurrencyOnChainGatewayModel>? {
        var resp = currencyImplementationRepository.findGateways(
            request?.currencySymbol,
            request?.gatewayUuid,
            request?.chain,
            request?.currencyImplementationName
        )
        return resp
            ?: throw OpexError.ImplNotFound.exception()
    }

    private suspend fun loadImpl(gateway: String, symbol: String): Mono<CurrencyOnChainGatewayModel>? {
        return currencyImplementationRepository.findByGatewayUuidAndCurrencySymbol(gateway, symbol)
            ?: throw OpexError.ImplNotFound.exception()
    }

    private suspend fun doSave(request: CurrencyOnChainGatewayModel): CurrencyOnChainGatewayModel? {
        return currencyImplementationRepository.save(request).awaitSingleOrNull()
    }

    override suspend fun changeWithdrawStatus(symbol: String, chain: String, status: Boolean) {
        val onChainGateway =
            currencyImplementationRepository.findByCurrencySymbolAndChain(symbol, chain).awaitSingleOrNull()
                ?: throw OpexError.TokenNotFound.exception()

        onChainGateway.apply {
            withdrawAllowed = status
            currencyImplementationRepository.save(onChainGateway).awaitFirstOrNull()
        }
    }

    override suspend fun getWithdrawData(symbol: String, network: String): WithdrawData {
        return currencyImplementationRepository.findWithdrawDataBySymbolAndChain(symbol, network)
            .awaitSingleOrNull() ?: throw OpexError.CurrencyNotFound.exception()
    }

    override suspend fun fetchGatewayWithoutSymbol(
        chain: String,
        isToken: Boolean,
        tokenAddress: String?
    ): CryptoCurrencyCommand? {
        chainRepository.findByName(chain)?.awaitFirstOrElse { throw OpexError.ChainNotFound.exception() }

        return if (isToken)
            currencyImplementationRepository.findTokenGateway(chain, tokenAddress!!).awaitSingleOrNull()?.toDto()
        else
            currencyImplementationRepository.findMainAssetGateway(chain).awaitSingleOrNull()?.toDto()
    }
}
