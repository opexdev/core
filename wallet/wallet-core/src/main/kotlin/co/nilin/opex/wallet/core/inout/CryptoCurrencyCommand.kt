package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.util.UUID

data class CryptoCurrencyCommand(
        var currencyUUID: String,
        var currencyImpUuid: String? = UUID.randomUUID().toString(),
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
)