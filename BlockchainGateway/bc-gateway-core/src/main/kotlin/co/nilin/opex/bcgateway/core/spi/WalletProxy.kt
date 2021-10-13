package co.nilin.opex.bcgateway.core.spi

import java.math.BigDecimal

interface WalletProxy {
    suspend fun transfer(uuid: String, symbol: String, amount: BigDecimal)
}
