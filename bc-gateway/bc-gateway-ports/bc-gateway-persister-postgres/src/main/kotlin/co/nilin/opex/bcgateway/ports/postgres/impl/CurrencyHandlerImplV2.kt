package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.CurrencyImplementationRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.CurrencyOnChainGatewayLocalizationRepository
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayLocalizationModel
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayModel
import co.nilin.opex.bcgateway.ports.postgres.util.toCommand
import co.nilin.opex.bcgateway.ports.postgres.util.toDto
import co.nilin.opex.bcgateway.ports.postgres.util.toModel
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getDefaultUserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@Component
class CurrencyHandlerImplV2(
    private val chainRepository: ChainRepository,
    private val currencyImplementationRepository: CurrencyImplementationRepository,
    private val currencyOnChainGatewayLocalizationRepository: CurrencyOnChainGatewayLocalizationRepository,
    private val transactionalOperator: TransactionalOperator
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
        return doSave(request)?.toDto();
    }

    override suspend fun updateOnChainGateway(request: CryptoCurrencyCommand): CryptoCurrencyCommand? {
        return loadImpls(FetchGateways(gatewayUuid = request.gatewayUuid, currencySymbol = request.currencySymbol))
            ?.awaitFirstOrElse { throw OpexError.GatewayNotFount.exception() }?.let { oldGateway ->
                doUpdate(oldGateway.toDto().updateTo(request).toModel().apply { id = oldGateway.id })?.toDto()
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
        return loadImpls(
            data,
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )?.map { it.toDto() }
            ?.collect(Collectors.toList())?.awaitFirstOrNull()
    }

    override suspend fun fetchOnChainGateway(gatewayUuid: String, symbol: String): CryptoCurrencyCommand? {
        return loadImpl(
            gatewayUuid,
            symbol,
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )?.awaitFirstOrNull()?.toDto()
    }

    private suspend fun loadImpls(
        request: FetchGateways?,
        language: String? = null
    ): Flux<CurrencyOnChainGatewayView>? {
        var resp = currencyImplementationRepository.findGateways(
            request?.currencySymbol,
            request?.gatewayUuid,
            request?.chain,
            request?.currencyImplementationName,
            language ?: getDefaultUserLanguage()
        )
        return resp
            ?: throw OpexError.ImplNotFound.exception()
    }

    private suspend fun loadImpl(
        gateway: String,
        symbol: String,
        language: String
    ): Mono<CurrencyOnChainGatewayView>? {
        return currencyImplementationRepository.findByGatewayUuidAndCurrencySymbol(
            gateway,
            symbol,
            language
        )
            ?: throw OpexError.ImplNotFound.exception()
    }

    private suspend fun doSave(request: CryptoCurrencyCommand): CurrencyOnChainGatewayView? {
        return transactionalOperator.executeAndAwait {

            val gateway = currencyImplementationRepository.save(request.toModel()).awaitSingleOrNull()
            if (gateway == null) {
                return@executeAndAwait null
            }

            if (!request.depositDescription.isNullOrEmpty() && !request.withdrawDescription.isNullOrEmpty()) {
                currencyOnChainGatewayLocalizationRepository.save(
                    CurrencyOnChainGatewayLocalizationModel(
                        gatewayId = gateway.id!!,
                        depositDescription = request.depositDescription,
                        withdrawDescription = request.withdrawDescription,
                        language = getDefaultUserLanguage()
                    )
                ).awaitSingleOrNull()
            }
            loadImpl(gateway.gatewayUuid, gateway.currencySymbol, getDefaultUserLanguage())?.awaitFirstOrNull()

        }
    }

    private suspend fun doUpdate(request: CurrencyOnChainGatewayModel): CurrencyOnChainGatewayView? {
        val gateway = currencyImplementationRepository.save(request).awaitSingleOrNull() ?: return null
        return loadImpl(gateway.gatewayUuid, gateway.currencySymbol, getDefaultUserLanguage())?.awaitFirstOrNull()
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

    override suspend fun saveOnChainGatewayLocalization(
        gatewayUuid: String,
        localizations: List<CurrencyOnChainGatewayLocalizationCommand>
    ): List<CurrencyOnChainGatewayLocalizationCommand> {
        return transactionalOperator.executeAndAwait {

            val gateway = currencyImplementationRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()
                ?: throw OpexError.GatewayNotFount.exception()

            localizations.forEach { g ->
                if (!g.depositDescription.isNullOrBlank() || !g.withdrawDescription.isNullOrBlank())
                    currencyOnChainGatewayLocalizationRepository.upsert(
                        gatewayId = gateway.id!!,
                        depositDescription = g.depositDescription,
                        withdrawDescription = g.withdrawDescription,
                        language = UserLanguage.safeValueOf(g.language).toString()
                    ).awaitSingleOrNull()
            }

            currencyOnChainGatewayLocalizationRepository.findByGatewayId(gateway.id!!)
                .map { it.toCommand() }
                .collectList()
                .awaitSingleOrNull()
                ?: emptyList()
        } ?: throw OpexError.BadRequest.exception("Failed to save gateway localizations")
    }


    override suspend fun fetchOnChainGatewayLocalizations(gatewayUuid: String): List<CurrencyOnChainGatewayLocalizationCommand> {
        val gateway = currencyImplementationRepository.findByGatewayUuid(gatewayUuid)?.awaitSingleOrNull()
            ?: throw OpexError.GatewayNotFount.exception()
        return currencyOnChainGatewayLocalizationRepository.findByGatewayId(gateway.id!!)
            .map { it.toCommand() }
            .collectList()
            .awaitSingleOrNull()
            ?: emptyList()
    }

    override suspend fun deleteOnChainGatewayLocalizations(id: Long) {
        currencyOnChainGatewayLocalizationRepository.deleteById(id).awaitSingleOrNull()
    }
}
