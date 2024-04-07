package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.otc.FetchCurrencyInfo
import java.math.BigDecimal
import java.util.UUID

data class CurrencyCommand(
        var symbol: String,
        var uuid:String?=UUID.randomUUID().toString(),
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
        var isWithdrawEnable: Boolean? = true,
        var withdrawFee: BigDecimal?,
        var depositMethods:List<DepositMethod>?,
        var withdrawMethods:List<WhithdrawMethod>?,
        var externalUrl:String?=null,
        var isCryptoCurrency:Boolean?=false,
        var impls:List<CryptoCurrencyCommand>

)
