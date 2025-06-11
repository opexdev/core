package co.nilin.opex.wallet.core.service

import java.math.BigDecimal

interface PrecisionService {
    fun calculatePrecision(amount: BigDecimal, symbol: String): BigDecimal

    fun validatePrecision(amount: BigDecimal, symbol: String)

    fun getPrecision(symbol: String): BigDecimal
}