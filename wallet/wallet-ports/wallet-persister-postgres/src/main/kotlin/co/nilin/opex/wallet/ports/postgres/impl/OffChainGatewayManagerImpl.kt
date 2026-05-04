package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getDefaultUserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.GatewayData
import co.nilin.opex.wallet.core.inout.OffChainGatewayCommand
import co.nilin.opex.wallet.core.model.FetchGateways
import co.nilin.opex.wallet.core.spi.GatewayPersister
import co.nilin.opex.wallet.ports.postgres.dao.OffChainGatewayLocalizationRepository
import co.nilin.opex.wallet.ports.postgres.dao.OffChainGatewayRepository
import co.nilin.opex.wallet.ports.postgres.dto.OffChainGatewayView
import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayLocalizationModel
import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service("offChainGateway")
class OffChainGatewayManagerImpl(
    private val offChainGatewayRepository: OffChainGatewayRepository,
    private val offChainGatewayLocalizationRepository: OffChainGatewayLocalizationRepository,
    private val transactionalOperator: TransactionalOperator
) : GatewayPersister {
    override suspend fun createGateway(
        currencyGateway: CurrencyGatewayCommand, internalToken: String?
    ): CurrencyGatewayCommand? {
        val input = currencyGateway as OffChainGatewayCommand
        offChainGatewayRepository.findByCurrencySymbolAndAndTransferMethod(input.currencySymbol!!, input.transferMethod)
            ?.awaitFirstOrNull()?.let { throw OpexError.GatewayIsExist.exception() }
        return _save(currencyGateway)?.toDto()
    }

    override suspend fun updateGateway(
        currencyGateway: CurrencyGatewayCommand, internalToken: String?
    ): CurrencyGatewayCommand? {
        val oldGateway = _fetchGateway(currencyGateway.currencySymbol!!, currencyGateway.gatewayUuid!!)
            ?: throw OpexError.GatewayNotFount.exception()
        return _update((currencyGateway as OffChainGatewayCommand).toModel().apply { id = oldGateway.id })?.toDto()
        return null
    }

    override suspend fun fetchGateways(symbol: String?, internalToken: String?): List<CurrencyGatewayCommand>? {
        return _fetchGateways(
            FetchGateways(currencySymbol = symbol),
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )?.map { it.toDto() }
    }

    override suspend fun fetchGatewayDetail(
        gatewayUuid: String, currencySymbol: String, internalToken: String?
    ): CurrencyGatewayCommand? {
        return _fetchGateway(
            currencySymbol, gatewayUuid, UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )?.toDto()
    }

    override suspend fun deleteGateway(gatewayUuid: String, currencySymbol: String, internalToken: String?) {
        offChainGatewayRepository.findByGatewayUuidAndCurrencySymbol(gatewayUuid, currencySymbol)?.let {
            offChainGatewayRepository.deleteByGatewayUuid(gatewayUuid)?.awaitFirstOrNull()
        } ?: OpexError.GatewayNotFount.exception()


    }

    override suspend fun getWithdrawData(symbol: String, network: String): GatewayData {
        TODO("Not yet implemented")
    }


    private suspend fun _save(currencyGateway: CurrencyGatewayCommand): OffChainGatewayView? {
        return transactionalOperator.executeAndAwait {

            val input = currencyGateway as OffChainGatewayCommand
            val gateway = offChainGatewayRepository.save(input.toModel()).awaitFirstOrNull()
                ?: throw OpexError.BadRequest.exception("Error in saving gateway")

            if (!currencyGateway.depositDescription.isNullOrEmpty() && !currencyGateway.withdrawDescription.isNullOrEmpty()) {
                offChainGatewayLocalizationRepository.save(
                    OffChainGatewayLocalizationModel(
                        gatewayId = gateway.id!!,
                        depositDescription = currencyGateway.depositDescription,
                        withdrawDescription = currencyGateway.withdrawDescription,
                        language = getDefaultUserLanguage()
                    )
                ).awaitSingleOrNull()
            }

            _fetchGateway(gateway.currencySymbol, gateway.gatewayUuid)
        }
    }

    private suspend fun _update(currencyGateway: OffChainGatewayModel): OffChainGatewayView? {
        val gateway = offChainGatewayRepository.save(currencyGateway).awaitFirstOrNull()
            ?: throw OpexError.BadRequest.exception("Error in saving gateway")
        return _fetchGateway(gateway.currencySymbol, gateway.gatewayUuid)
    }


    private suspend fun _fetchGateway(
        currencySymbol: String,
        gatewayUuid: String,
        language: String? = null,
    ): OffChainGatewayView? {
        return offChainGatewayRepository.findByGatewayUuidAndCurrencySymbol(
            gatewayUuid, currencySymbol, language ?: getDefaultUserLanguage()
        )?.awaitFirstOrNull()
    }

    private suspend fun _fetchGateways(
        fetchGateways: FetchGateways, language: String? = null
    ): List<OffChainGatewayView>? {
        return offChainGatewayRepository.findGateways(
            currencySymbol = fetchGateways.currencySymbol, language = language ?: getDefaultUserLanguage()
        )?.collectList()?.awaitFirstOrNull()
    }
}