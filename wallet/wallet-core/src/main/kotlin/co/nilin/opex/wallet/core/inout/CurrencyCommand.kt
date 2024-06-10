package co.nilin.opex.wallet.core.inout


import java.math.BigDecimal
import java.util.UUID

data class CurrencyCommand(
        var symbol: String,
        var uuid: String? = UUID.randomUUID().toString(),
        var name: String,
        var precision: BigDecimal,
        var title: String? = null,
        var alias: String? = null,
        var icon: String? = null,
        var isTransitive: Boolean? = false,
        var isActive: Boolean? = true,
        var sign: String? = null,
        var description: String? = null,
        var shortDescription: String? = null,
        var withdrawIsEnable: Boolean? = true,
        var depositIsEnable: Boolean? = true,
        var withdrawFee: BigDecimal?= BigDecimal.ZERO,
        var depositMethods: List<DepositMethod>?=null,
        var withdrawMethods: List<WithdrawMethod>?=null,
        var externalUrl: String? = null,
        var isCryptoCurrency: Boolean? = false,
        var impls: List<CryptoCurrencyCommand>?=null,


        ) {
    fun updateTo(newData: CurrencyCommand): CurrencyCommand {

            return newData.apply {
                this.uuid = uuid
                this.symbol = symbol
            }
        }



}


data class CurrenciesCommand(var currencies: List<CurrencyCommand>?)
