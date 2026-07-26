package co.nilin.opex.wallet.core.service

import java.math.BigDecimal

interface PrecisionService {
    fun calculatePrecision(amount: BigDecimal, symbol: String, allowSubPrecision: Boolean = true): BigDecimal

    suspend fun validatePrecision(amount: BigDecimal, symbol: String)

    suspend fun getPrecision(symbol: String): BigDecimal
}