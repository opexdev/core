package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import java.math.BigDecimal

interface CryptoCurrencyHandler {

    suspend fun addCurrency(name: String, symbol: String)

    suspend fun addCurrencyImplementationV2(
        currencySymbol: String,
        implementationSymbol: String,
        currencyName:String,
        chain: String,
        tokenName: String?,
        tokenAddress: String?,
        isToken: Boolean,
        withdrawFee: BigDecimal,
        minimumWithdraw: BigDecimal,
        isWithdrawEnabled: Boolean,
        decimal: Int
    ): CryptoCurrencyCommand?


    suspend fun updateCurrencyImplementation(
        currencySymbol: String,
        implementationSymbol: String,
        currencyName:String,
        newChain: String?=null,
        tokenName: String?,
        tokenAddress: String?,
        isToken: Boolean,
        withdrawFee: BigDecimal,
        minimumWithdraw: BigDecimal,
        isWithdrawEnabled: Boolean,
        decimal: Int,
        chain:String
    ): CryptoCurrencyCommand?


    suspend fun editCurrency(name: String, symbol: String)

    suspend fun deleteCurrency(name: String)

    suspend fun addCurrencyImplementation(
        currencySymbol: String,
        implementationSymbol: String,
        chain: String,
        tokenName: String?,
        tokenAddress: String?,
        isToken: Boolean,
        withdrawFee: BigDecimal,
        minimumWithdraw: BigDecimal,
        isWithdrawEnabled: Boolean,
        decimal: Int
    ): CryptoCurrencyCommand

//    suspend fun fetchAllImplementations(): CurrencyImps

    suspend fun fetchCurrencyInfo(symbol: String): CryptoCurrencyCommand

    suspend fun findByChainAndTokenAddress(chain: String, address: String?): CryptoCurrencyCommand?

    suspend fun findImplementationsWithTokenOnChain(chain: String): List<CryptoCurrencyCommand>

    suspend fun findImplementationsByCurrency(currency: String): List<CryptoCurrencyCommand>

    suspend fun changeWithdrawStatus(symbol: String, chain: String, status: Boolean)

}
