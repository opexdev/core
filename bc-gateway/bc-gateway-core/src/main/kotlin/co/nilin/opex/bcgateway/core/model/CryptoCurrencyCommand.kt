package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class CryptoCurrencyCommand(
        var currencySymbol: String,
        var gatewayUuid: String?,
        var implementationSymbol: String? = currencySymbol,
        var isActive: Boolean? = true,
        var isToken: Boolean? = false,
        var tokenName: String? = null,
        var tokenAddress: String? = null,
        var withdrawFee: BigDecimal?,
        var withdrawAllowed: Boolean? = true,
        var depositAllowed: Boolean? = true,
        val withdrawMin: BigDecimal? = BigDecimal.ZERO,
        var withdrawMax: BigDecimal? = BigDecimal.ZERO,
        var depositMin: BigDecimal? = BigDecimal.ZERO,
        var depositMax: BigDecimal? = BigDecimal.ZERO,
        var decimal: Int,
        var chain: String,
        var type: String= "OnChain"

//        var chainDetail: Chain? = null


) {
    fun updateTo(newData: CryptoCurrencyCommand): CryptoCurrencyCommand {
        return newData.apply {
            this.currencySymbol = currencySymbol
            this.gatewayUuid = gatewayUuid
        }
    }

}


