package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways
import co.nilin.opex.wallet.core.service.GatewayService
import co.nilin.opex.wallet.core.spi.GatewayPersister
import co.nilin.opex.wallet.ports.postgres.dao.ManualGatewayRepository
import org.springframework.stereotype.Service

@Service("manualGateway")
class ManualGatewayManagerImpl (private val manualGatewayRepository: ManualGatewayRepository): GatewayPersister {
    override suspend fun createGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {


    }

    override suspend fun updateGateway(currencyImp: CurrencyGatewayCommand, internalToken: String?): CurrencyGatewayCommand? {
        TODO("Not yet implemented")
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
}