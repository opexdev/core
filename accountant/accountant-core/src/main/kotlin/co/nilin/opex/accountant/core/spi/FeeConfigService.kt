package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.FeeConfig
import co.nilin.opex.accountant.core.model.UserFee
import java.math.BigDecimal

interface FeeConfigService {

    suspend fun loadFeeConfigs(): List<FeeConfig>
    suspend fun saveFee(config: FeeConfig)
    suspend fun updateFee(config: FeeConfig)
    suspend fun loadFeeConfig(name: String): FeeConfig
    suspend fun loadMatchingFeeConfig(assetVolume: BigDecimal, tradeVolume: BigDecimal): UserFee

}