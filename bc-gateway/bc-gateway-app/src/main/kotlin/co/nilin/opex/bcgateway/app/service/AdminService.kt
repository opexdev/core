package co.nilin.opex.bcgateway.app.service

import co.nilin.opex.bcgateway.app.dto.AddChainRequest
import co.nilin.opex.bcgateway.app.dto.AssetRequest
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.spi.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val chainLoader: ChainLoader,
    private val currencyHandler: CurrencyHandler,
    private val chainScheduler: ChainSyncSchedulerHandler,
    private val addressTypeHandler: AddressTypeHandler,
    private val chainEndpointHandler: ChainEndpointHandler,
) {

    suspend fun addCurrency(name: String, symbol: String) {
        currencyHandler.addCurrency(name, symbol)
    }

    suspend fun editCurrency(name: String, symbol: String) {
        currencyHandler.editCurrency(name, symbol)
    }

    suspend fun deleteCurrency(name: String) {
        currencyHandler.deleteCurrency(name)
    }

    @Transactional
    suspend fun addChain(body: AddChainRequest) {
        val chain = chainLoader.addChain(body.name!!, body.addressType!!)
        chainScheduler.scheduleChain(chain.name, body.scheduleDelaySeconds, body.scheduleErrorDelaySeconds)
        if (body.scannerEndpoint != null)
            chainEndpointHandler.addEndpoint(chain.name, body.scannerEndpoint)
    }

    suspend fun addAddressType(name: String, addressRegex: String, memoRegex: String?) {
        addressTypeHandler.addAddressType(name, addressRegex, memoRegex)
    }

    suspend fun addAsset(body: AssetRequest): CurrencyImplementation {
        return with(body) {
            currencyHandler.addCurrencyImplementation(
                symbol!!,
                chain!!,
                tokenName,
                tokenAddress,
                isToken,
                withdrawFee,
                minimumWithdraw,
                isWithdrawEnabled,
                decimal
            )
        }
    }

}