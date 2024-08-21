package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways
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
        val oldGateway = _fetchGateway(currencyGateway.gatewayUuid!!) ?: throw OpexError.GatewayNotFount.exception()
        return _save((currencyGateway as OffChainGatewayCommand).toModel().apply { id = oldGateway.id })?.toDto()
    }

    override suspend fun fetchGateways(symbol: String?, internalToken: String?): CurrencyGateways? {
        TODO("Not yet implemented")
    }

    override suspend fun fetchGatewayDetail(implUuid: String, currencySymbol: String, internalToken: String?): CurrencyGatewayCommand? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGateway(implUuid: String, currencySymbol: String, internalToken: String?) {
        TODO("Not yet implemented")
    }


    private suspend fun _save(currencyGateway: OffChainGatewayModel): OffChainGatewayModel? {
        return offChainGatewayRepository.save(currencyGateway)?.awaitFirstOrNull()
    }

    private suspend fun _fetchGateway(gatewayUuid: String): OffChainGatewayModel? {
        return offChainGatewayRepository.findByGatewayUuid(gatewayUuid)?.awaitFirstOrNull()
    }

    private suspend fun _fetchGateways(fetchGateways: FetchGateways): List<OffChainGatewayModel>? {
        return offChainGatewayRepository.findGateways(fetchGateways.currencySymbol,fetchGateways.gatewayUuid)?.collectList()?.awaitFirstOrNull()
    }
}