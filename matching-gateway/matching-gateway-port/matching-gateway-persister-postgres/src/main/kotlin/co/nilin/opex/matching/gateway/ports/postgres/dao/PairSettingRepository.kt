package co.nilin.opex.matching.gateway.ports.postgres.dao

import co.nilin.opex.matching.gateway.ports.postgres.model.PairSettingModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PairSettingRepository : ReactiveCrudRepository<PairSettingModel, String> {
    fun findByPair(pair: String): Mono<PairSettingModel>

    @Query("insert into pair_setting(pair,is_available) values(:pair,:isAvailable) ")
    fun insert(pair: String, isAvailable: Boolean): Mono<PairSettingModel>
}
