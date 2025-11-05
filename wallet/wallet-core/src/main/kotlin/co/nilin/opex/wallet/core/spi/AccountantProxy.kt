package co.nilin.opex.wallet.core.spi

import java.math.BigDecimal

interface AccountantProxy {

    suspend fun canRequestWithdraw(
        uuid: String,
        userLevel: String,
        currency: String,
        amount: BigDecimal
    ) : Boolean
}