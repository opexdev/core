package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.FeeConfig
import co.nilin.opex.accountant.core.spi.FeeConfigService
import co.nilin.opex.accountant.ports.postgres.dao.FeeConfigRepository
import co.nilin.opex.accountant.ports.postgres.model.FeeConfigModel
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class FeeConfigServiceImpl(
    private val feeConfigRepository: FeeConfigRepository,
) : FeeConfigService {

    override suspend fun loadFeeConfigs(): List<FeeConfig> {
        return feeConfigRepository.findAllByOrder()
            .collectList()
            .awaitFirstOrElse { emptyList() }

    }

    override suspend fun saveFee(config: FeeConfig) {
        feeConfigRepository.findByName(config.name).awaitFirstOrNull()?.let {
            throw OpexError.FeeConfigAlreadyExists.exception("FeeConfig with name '${config.name}' already exists")
        }
        feeConfigRepository.save(config.asFeeConfigModel()).awaitFirst()
    }

    override suspend fun updateFee(config: FeeConfig) {
        feeConfigRepository.findByName(config.name).awaitFirstOrNull()
            ?: throw OpexError.FeeConfigNotFound.exception("FeeConfig with name '${config.name}' not found")

        feeConfigRepository.save(config.asFeeConfigModel()).awaitFirst()
    }

    override suspend fun loadFeeConfig(name: String): FeeConfig {
        return feeConfigRepository.findByName(name).awaitFirstOrNull()
            ?: throw OpexError.FeeConfigNotFound.exception("FeeConfig with name '${name}' not found")
    }

    override suspend fun loadMatchingFeeConfig(
        assetVolume: BigDecimal,
        tradeVolume: BigDecimal
    ): FeeConfig {
        return feeConfigRepository.findMatchingConfig(assetVolume, tradeVolume).awaitFirst()
    }

    private fun FeeConfig.asFeeConfigModel() =
        FeeConfigModel(
            name,
            displayOrder,
            minAssetVolume,
            maxAssetVolume,
            minTradeVolume,
            maxTradeVolume,
            makerFee,
            takerFee,
            condition
        )
}