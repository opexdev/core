package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.ManualGatewayCommand
import co.nilin.opex.wallet.core.inout.GatewayData
import co.nilin.opex.wallet.core.model.FetchGateways
import co.nilin.opex.wallet.core.spi.GatewayPersister
import co.nilin.opex.wallet.ports.postgres.dao.ManualGatewayRepository
import co.nilin.opex.wallet.ports.postgres.model.ManualGatewayModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service("manualGateway")
class ManualGatewayManagerImpl(private val manualGatewayRepository: ManualGatewayRepository) : GatewayPersister {
    override suspend fun createGateway(currencyGateway: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        return _save((currencyGateway as ManualGatewayCommand).toModel())?.toDto()
    }

    override suspend fun updateGateway(currencyGateway: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        val oldGateway = _fetchGateway(currencyGateway.currencySymbol!!, currencyGateway.gatewayUuid!!)
                ?: throw OpexError.GatewayNotFount.exception()
        return _save((currencyGateway as ManualGatewayCommand).toModel().apply { id = oldGateway.id })?.toDto()
    }

    override suspend fun fetchGateways(symbol: String?, internalToken: String?): List<CurrencyGatewayCommand>? {
        return _fetchGateways(FetchGateways(currencySymbol = symbol))?.map { it.toDto() }
    }


    override suspend fun fetchGatewayDetail(gatewayUuid: String, currencySymbol: String, internalToken: String?): CurrencyGatewayCommand? {
        return _fetchGateway(currencySymbol, gatewayUuid)?.toDto()
    }


    override suspend fun deleteGateway(gatewayUuid: String, currencySymbol: String, internalToken: String?) {
        manualGatewayRepository.findByGatewayUuidAndCurrencySymbol(gatewayUuid, currencySymbol)?.let {
            manualGatewayRepository.deleteByGatewayUuid(gatewayUuid)?.awaitFirstOrNull()
        } ?: OpexError.GatewayNotFount.exception()
    }


    override suspend fun getWithdrawData(symbol: String, network: String): GatewayData {
        TODO("Not yet implemented")
    }

    private suspend fun _save(currencyGateway: ManualGatewayModel): ManualGatewayModel? {
        return manualGatewayRepository.save(currencyGateway)?.awaitFirstOrNull()
    }


    private suspend fun _fetchGateway(currencySymbol: String, gatewayUuid: String): ManualGatewayModel? {
        return manualGatewayRepository.findByGatewayUuidAndCurrencySymbol(gatewayUuid, currencySymbol)?.awaitFirstOrNull()
    }

    private suspend fun _fetchGateways(fetchGateways: FetchGateways): List<ManualGatewayModel>? {
        return manualGatewayRepository.findGateways(fetchGateways.currencySymbol, fetchGateways.gatewayUuid)?.collectList()?.awaitFirstOrNull()
    }
}