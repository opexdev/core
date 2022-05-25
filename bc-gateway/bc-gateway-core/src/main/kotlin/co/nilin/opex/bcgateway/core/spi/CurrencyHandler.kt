package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo
import java.math.BigDecimal

interface CurrencyHandler {

    suspend fun addCurrency(name: String, symbol: String)

    suspend fun editCurrency(name: String, symbol: String)

    suspend fun deleteCurrency(name: String)

    suspend fun addCurrencyImplementation(
        symbol: String,
        chain: String,
        tokenName: String?,
        tokenAddress: String?,
        isToken: Boolean,
        withdrawFee: BigDecimal,
        minimumWithdraw: BigDecimal,
        isWithdrawEnabled: Boolean,
        decimal: Int
    ): CurrencyImplementation

    suspend fun fetchAllImplementations(): List<CurrencyImplementation>

    suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo

    suspend fun findByChainAndTokenAddress(chain: String, address: String?): CurrencyImplementation?

    suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation>

    suspend fun changeWithdrawStatus(symbol: String, chain: String, status: Boolean)

}