package co.nilin.opex.bcgateway.app.service

import co.nilin.opex.bcgateway.app.dto.AddChainRequest
import co.nilin.opex.bcgateway.app.dto.TokenRequest
import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.spi.AddressTypeHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
        private val chainLoader: ChainLoader,
        private val currencyHandler: CryptoCurrencyHandlerV2,
        private val addressTypeHandler: AddressTypeHandler
) {

//    suspend fun addCurrency(name: String, symbol: String) {
//        currencyHandler.addCurrency(name, symbol)
//    }
//
//    suspend fun editCurrency(name: String, symbol: String) {
//        currencyHandler.editCurrency(name, symbol)
//    }
//
//    suspend fun deleteCurrency(name: String) {
//        currencyHandler.deleteCurrency(name)
//    }

    @Transactional
    suspend fun addChain(body: AddChainRequest) {
        chainLoader.addChain(body.name!!, body.addressType!!)
    }

    suspend fun addAddressType(name: String, addressRegex: String, memoRegex: String?) {
        addressTypeHandler.addAddressType(name, addressRegex, memoRegex)
    }

//    suspend fun addToken(body: TokenRequest): CryptoCurrencyCommand {
//        return with(body) {
//            currencyHandler.addCurrencyImplementation(
//                currencySymbol!!,
//                implementationSymbol ?: currencySymbol,
//                chain!!,
//                tokenName,
//                tokenAddress,
//                isToken,
//                withdrawFee,
//                minimumWithdraw,
//                isWithdrawEnabled,
//                decimal
//            )
//        }
//    }

//    suspend fun changeTokenWithdrawStatus(symbol: String, chain: String, status: Boolean) {
//        currencyHandler.changeWithdrawStatus(symbol, chain, status)
//    }

}
