package co.nilin.opex.wallet.core.model

import co.nilin.opex.wallet.core.model.otc.CurrencyImplementationResponse
import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
import java.math.BigDecimal


data class Currency(
        var symbol: String,
        var name: String,
        var precision: BigDecimal,
        var title: String? = null,
        var alias: String? = null,
        var maxDeposit: BigDecimal? = BigDecimal.TEN,
        var minDeposit: BigDecimal? = BigDecimal.ZERO,
        var minWithdraw: BigDecimal? = BigDecimal.TEN,
        var maxWithdraw: BigDecimal? = BigDecimal.ZERO,
        var icon: String? = null,
        var isTransitive: Boolean? = false,
        var isActive: Boolean? = true,
        var sign: String? = null,
        var description: String? = null,
        var shortDescription: String? = null,
        var currencyImpData: FetchCurrencyInfo? = null
)


data class Currencies(var currencies: List<Currency>?)


data class CurrencyImp(
        var symbol: String,
        var name: String,
        var precision: BigDecimal,
        var title: String? = null,
        var alias: String? = null,
        var maxDeposit: BigDecimal? = BigDecimal.TEN,
        var minDeposit: BigDecimal? = BigDecimal.ZERO,
        var minWithdraw: BigDecimal? = BigDecimal.TEN,
        var maxWithdraw: BigDecimal? = BigDecimal.ZERO,
        var icon: String? = null,
        var isTransitive: Boolean? = false,
        var isActive: Boolean? = true,
        var sign: String? = null,
        var description: String? = null,
        var shortDescription: String? = null,
        var implementationSymbol: String?,
        var newChain: String? = null,
        var tokenName: String?,
        var tokenAddress: String?,
        var isToken: Boolean? = false,
        var withdrawFee: BigDecimal?,
        var isWithdrawEnabled: Boolean? = true,
        var decimal: Int?,
        var chain: String?
) {
    fun isValidForPropagatingOnChain(): Boolean {
        return (chain != null && implementationSymbol != null && decimal != null && withdrawFee!=null && minWithdraw !=null)

    }
}

    data class PropagateCurrencyChanges(

            var currencySymbol: String,
            var currencyName: String,
            var implementationSymbol: String,
            var newChain: String? = null,
            var tokenName: String?,
            var tokenAddress: String?,
            var isToken: Boolean? = false,
            var withdrawFee: BigDecimal,
            var minimumWithdraw: BigDecimal,
            var isWithdrawEnabled: Boolean? = true,
            var decimal: Int,
            var chain: String
    )