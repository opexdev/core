package co.nilin.opex.wallet.core.model

import java.math.BigDecimal


data class Currency(var symbol: String,
                    var name: String,
                    var precision: BigDecimal,
                    var title: String? = null,
                    var alias: String? = null,
                    var maxDeposit: BigDecimal? = BigDecimal.TEN,
                    var minDeposit: BigDecimal? = BigDecimal.ZERO,
                    var minWithdraw: BigDecimal? = BigDecimal.TEN,
                    var maxWithdraw: BigDecimal? = BigDecimal.ZERO,
                    var icon: String? = null,
                    var isTransitive: Boolean? = false)


data class Currencies(var currencies: List<Currency>?)