package co.nilin.opex.matching.gateway.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.CacheManager
import co.nilin.opex.matching.gateway.ports.postgres.dao.PairCategoryRepository
import co.nilin.opex.matching.gateway.ports.postgres.dao.PairSettingRepository
import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting
import co.nilin.opex.matching.gateway.ports.postgres.model.PairCategoryModel
import co.nilin.opex.matching.gateway.ports.postgres.service.PairSettingService
import co.nilin.opex.matching.gateway.ports.postgres.util.toPairSetting
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class PairSettingServiceImpl(
    private val pairSettingRepository: PairSettingRepository,
    private val pairCategoryRepository: PairCategoryRepository,
    @Qualifier("appCacheManager") private val cacheManager: CacheManager<String, PairSetting>
) : PairSettingService {

    override suspend fun load(pair: String): PairSetting {
        return cacheManager.get("pair-setting:$pair")
            ?: pairSettingRepository.findByPair(pair)
                .awaitFirstOrNull()?.let { pairSettingModel ->
                    val categories = pairCategoryRepository.findByPair(pairSettingModel.pair)
                        .map { it.category }
                        .toList()
                    pairSettingModel.categories = categories
                    pairSettingModel
                }?.let {
                    it.toPairSetting().also {
                        cacheManager.put(
                            "pair-setting:${it.pair}",
                            it,
                            5, TimeUnit.MINUTES
                        )
                    }
                }
            ?: throw OpexError.PairNotFound.exception()
    }

    override suspend fun loadAll(): List<PairSetting> {
        val pairSettings = pairSettingRepository.findAll().collectList().awaitFirst()

        if (pairSettings.isEmpty()) {
            return emptyList()
        }

        val categoriesByPair = pairCategoryRepository.findAll()
            .toList()
            .groupBy(
                keySelector = { it.pair },
                valueTransform = { it.category }
            )

        return pairSettings.map { ps ->
            ps.categories = categoriesByPair[ps.pair] ?: emptyList()
            ps.toPairSetting()
        }
    }

    override suspend fun update(pairSetting: PairSetting): PairSetting {
        val existing = pairSettingRepository.findByPair(pairSetting.pair)
            .awaitFirstOrNull()
            ?: throw OpexError.PairNotFound.exception()

        existing.apply {
            isAvailable = pairSetting.isAvailable
            minOrder = pairSetting.minOrder
            maxOrder = pairSetting.maxOrder
            orderTypes = pairSetting.orderTypes
            updateDate = LocalDateTime.now()
            internalChart = pairSetting.internalChart
            globalChart = pairSetting.globalChart
        }

        val saved = pairSettingRepository.save(existing)
            .awaitFirst()

        pairCategoryRepository.deleteByPair(pairSetting.pair).awaitFirstOrNull()
        pairSetting.categories.forEach { category ->
            pairCategoryRepository.save(
                PairCategoryModel(
                    pair = pairSetting.pair,
                    category = category
                )
            )
        }

        return saved.apply {
            categories = pairSetting.categories
        }.toPairSetting().also {
            cacheManager.put(
                "pair-setting:${it.pair}",
                it,
                5,
                TimeUnit.MINUTES
            )
        }
    }
}