package co.nilin.opex.bcgateway.core.spi

import java.math.BigDecimal

interface WalletProxy {
    fun transfer(uuid: String, symbol: String, amount: BigDecimal)
}