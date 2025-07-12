package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.service.PrecisionService
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.ports.postgres.util.RedisCacheHelper
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class PrecisionServiceImpl(
    private val redisCacheHelper: RedisCacheHelper, private val currencyServiceManager: CurrencyServiceManager
) : PrecisionService {

    //TODO optimize this
    override fun calculatePrecision(amount: BigDecimal, symbol: String): BigDecimal {
        val precision = redisCacheHelper.get<BigDecimal>("$symbol-precision")?.toInt() ?: return amount

        val scaledAmount = amount.setScale(precision, RoundingMode.DOWN)
        if (scaledAmount != BigDecimal.ZERO.setScale(precision)) {
            return scaledAmount
        }
        val decimalPart = amount.stripTrailingZeros().toPlainString().substringAfter('.', "")

        val zeroPrefixCount = decimalPart.indexOfFirst { it != '0' }.takeIf { it >= 0 } ?: 0
        val rest = decimalPart.drop(zeroPrefixCount)

        var nonZeroCount = 0
        var digitsToKeep = 0
        for (c in rest) {
            digitsToKeep++
            if (c != '0') nonZeroCount++
            if (nonZeroCount == 2) break
        }
        val totalScale = zeroPrefixCount + digitsToKeep

        return amount.setScale(totalScale, RoundingMode.DOWN)
    }


    override suspend fun validatePrecision(amount: BigDecimal, symbol: String) {
        val precision = redisCacheHelper.get<BigDecimal>("$symbol-precision") ?: (currencyServiceManager.fetchCurrency(
            FetchCurrency(symbol = symbol)
        )?.precision ?: throw OpexError.CurrencyNotFound.exception())

        val actualScale = amount.stripTrailingZeros().scale()

        if (actualScale > precision.toInt()) {
            throw OpexError.InvalidAmount.exception("Amount $amount exceeds allowed precision for $symbol (max $precision decimal places)")
        }
    }

    override suspend fun getPrecision(symbol: String): BigDecimal {
        return redisCacheHelper.get<BigDecimal>("$symbol-precision") ?: (currencyServiceManager.fetchCurrency(
            FetchCurrency(symbol = symbol)
        )?.precision ?: throw OpexError.CurrencyNotFound.exception())
    }

}