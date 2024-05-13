package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal
import java.util.*

data class CryptoCurrencyCommand(

        var currencyUUID: String,
        var currencyImpUuid: String,
        var implementationSymbol: String?,
        var tokenName: String?,
        var tokenAddress: String?,
        var isActive: Boolean? = true,
        var isToken: Boolean? = false,
        var withdrawFee: BigDecimal?,
        var isWithdrawEnable: Boolean? = true,
        var isDepositEnable: Boolean? = true,
        var decimal: Int?,
        var chain: String?
) {
    fun updateTo(newData: CryptoCurrencyCommand): CryptoCurrencyCommand {
        return newData.apply {
            this.currencyUUID = currencyUUID
            this.currencyImpUuid = currencyImpUuid
        }
    }



}



data class CurrencyImps(var imps: List<CryptoCurrencyCommand>?)
