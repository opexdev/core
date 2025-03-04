package co.nilin.opex.matching.gateway.ports.postgres.service

import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting

interface PairSettingService {
    suspend fun load(pair: String): PairSetting
    suspend fun loadAll(): List<PairSetting>
    suspend fun update(pair: String, isAvailable: Boolean): PairSetting
}