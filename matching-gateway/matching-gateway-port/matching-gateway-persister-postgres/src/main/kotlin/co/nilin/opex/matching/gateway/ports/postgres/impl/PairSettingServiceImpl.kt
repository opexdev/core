package co.nilin.opex.matching.gateway.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.matching.gateway.ports.postgres.dao.PairSettingRepository
import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting
import co.nilin.opex.matching.gateway.ports.postgres.service.PairSettingService
import co.nilin.opex.matching.gateway.ports.postgres.util.RedisCacheHelper
import co.nilin.opex.matching.gateway.ports.postgres.util.toPairSetting
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class PairSettingServiceImpl(
    private val pairSettingRepository: PairSettingRepository,
    private val redisCacheHelper: RedisCacheHelper
) : PairSettingService {

    override suspend fun load(pair: String): PairSetting {
        return redisCacheHelper.get<PairSetting>("pair-setting:$pair")
            ?: pairSettingRepository.findByPair(pair)
                .awaitFirstOrNull()
                ?.let {
                    it.toPairSetting().also {
                        redisCacheHelper.put(
                            "pair-setting:${it.pair}",
                            it,
                            DynamicInterval(5, TimeUnit.MINUTES)
                        )
                    }
                }
            ?: throw OpexError.PairNotFound.exception()
    }

    override suspend fun loadAll(): List<PairSetting> {
        return pairSettingRepository.findAll()
            .map { it.toPairSetting() }
            .collectList()
            .awaitFirstOrNull() ?: emptyList()
    }

    override suspend fun update(pair: String, isAvailable: Boolean): PairSetting {
        val pairSetting =
            pairSettingRepository.findByPair(pair).awaitFirstOrNull() ?: throw OpexError.PairNotFound.exception()
        if (pairSetting.isAvailable == isAvailable)
            throw OpexError.BadRequest.exception("Pair availability is already $isAvailable")
        pairSetting.apply {
            this.isAvailable = isAvailable
            this.updateDate = LocalDateTime.now()
        }
        return pairSettingRepository.save(pairSetting).awaitFirst().toPairSetting().also {
            redisCacheHelper.put(
                "pair-setting:${it.pair}",
                it,
                DynamicInterval(5, TimeUnit.MINUTES)
            )
        }
    }
}