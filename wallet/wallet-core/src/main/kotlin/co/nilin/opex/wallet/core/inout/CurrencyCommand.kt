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
        var isWithdrawEnable: Boolean? = true,
        var withdrawFee: BigDecimal?,
        var depositMethods: List<DepositMethod>?,
        var withdrawMethods: List<WithdrawMethod>?,
        var externalUrl: String? = null,
        var isCryptoCurrency: Boolean? = false,
        var impls: List<CryptoCurrencyCommand>

) {
    fun toUpdate(newData: CurrencyCommand): CurrencyCommand {

            return newData.apply {

                this.uuid = uuid
                this.symbol = symbol
            }
        }



}


data class CurrenciesCommand(var currencies: List<CurrencyCommand>?)
