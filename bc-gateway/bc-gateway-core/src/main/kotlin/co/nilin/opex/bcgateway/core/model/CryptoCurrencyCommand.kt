package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal
import java.util.*

data class CryptoCurrencyCommand(

        var currencySymbol: String,
        var implUuid: String?,
        var implementationSymbol: String,
        var isActive: Boolean? = true,
        var isToken: Boolean? = false,
        var tokenName: String?=null,
        var tokenAddress: String?=null,
        var withdrawFee: BigDecimal?,
        var withdrawAllowed: Boolean? = true,
        var depositAllowed: Boolean? = true,
        val withdrawMin: BigDecimal?= BigDecimal.ZERO,
        var decimal: Int,
        var chain: String,
        var chainDetail:Chain?=null





) {
    fun updateTo(newData: CryptoCurrencyCommand): CryptoCurrencyCommand {
        return newData.apply {
            this.currencySymbol = currencySymbol
            this.implUuid = implUuid
        }
    }

}



data class CurrencyImps(var imps: List<CryptoCurrencyCommand>?)
