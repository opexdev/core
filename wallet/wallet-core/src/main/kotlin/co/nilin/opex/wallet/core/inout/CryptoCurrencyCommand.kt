package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.util.UUID

data class CryptoCurrencyCommand (
        var currencyUUID: String,
        var currencyImpUuid:String?=UUID.randomUUID().toString(),
        var implementationSymbol: String?,
        var tokenName: String?,
        var tokenAddress: String?,
        var isToken: Boolean? = false,
        var withdrawFee: BigDecimal?,
        var isWithdrawEnabled: Boolean? = true,
        var decimal: Int?,
        var chain: String?
)