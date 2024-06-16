package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class CryptoCurrencyCommand(

        var currencySymbol: String,
        var currencyImpUuid: String,
        var implementationSymbol: String?,
        var tokenName: String?,
        var tokenAddress: String?,
        var isActive: Boolean? = true,
        var isToken: Boolean? = false,
        var withdrawFee: BigDecimal?,
        var withdrawAllowed: Boolean? = true,
        var depositAllowed: Boolean? = true,
        var decimal: Int?,
        var chain: String?
) {
    fun updateTo(newData: CryptoCurrencyCommand): CryptoCurrencyCommand {
        return newData.apply {
            this.currencySymbol = currencySymbol
            this.currencyImpUuid = currencyImpUuid
        }
    }



}



data class CurrencyImps(var imps: List<CryptoCurrencyCommand>?)
