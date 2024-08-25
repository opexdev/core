package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.OffChainGatewayCommand
import co.nilin.opex.wallet.core.model.FetchGateways
import co.nilin.opex.wallet.core.spi.GatewayPersister
import co.nilin.opex.wallet.ports.postgres.dao.OffChainGatewayRepository
import co.nilin.opex.wallet.ports.postgres.model.OffChainGatewayModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service("offChainGateway")
class OffChainGatewayManagerImpl(private val offChainGatewayRepository: OffChainGatewayRepository) : GatewayPersister {
    override suspend fun createGateway(currencyGateway: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        return _save((currencyGateway as OffChainGatewayCommand).toModel())?.toDto()
    }

    override suspend fun updateGateway(currencyGateway: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        val oldGateway = _fetchGateway(currencyGateway.currencySymbol!!,currencyGateway.gatewayUuid!!) ?: throw OpexError.GatewayNotFount.exception()
        return _save((currencyGateway as OffChainGatewayCommand).toModel().apply { id = oldGateway.id })?.toDto()
    }

    override suspend fun fetchGateways(symbol: String?, internalToken: String?): List<CurrencyGatewayCommand>? {
        return _fetchGateways(FetchGateways(currencySymbol = symbol))?.map { it.toDto() }
    }

    override suspend fun fetchGatewayDetail(gatewayUuid: String, currencySymbol: String, internalToken: String?): CurrencyGatewayCommand? {
        return _fetchGateway(currencySymbol, gatewayUuid)?.toDto()
    }

    override suspend fun deleteGateway(gatewayUuid: String, currencySymbol: String, internalToken: String?) {
        offChainGatewayRepository.findByGatewayUuidAndCurrencySymbol(gatewayUuid, currencySymbol)?.let {
            offChainGatewayRepository.deleteByGatewayUuid(gatewayUuid)?.awaitFirstOrNull()
        } ?: OpexError.GatewayNotFount.exception()


    }


    private suspend fun _save(currencyGateway: OffChainGatewayModel): OffChainGatewayModel? {
        return offChainGatewayRepository.save(currencyGateway)?.awaitFirstOrNull()
    }



    private suspend fun _fetchGateway(currencySymbol: String, gatewayUuid: String): OffChainGatewayModel? {
        return offChainGatewayRepository.findByGatewayUuidAndCurrencySymbol(gatewayUuid, currencySymbol)?.awaitFirstOrNull()
    }

    private suspend fun _fetchGateways(fetchGateways: FetchGateways): List<OffChainGatewayModel>? {
        return offChainGatewayRepository.findGateways(fetchGateways.currencySymbol)?.collectList()?.awaitFirstOrNull()
    }
}