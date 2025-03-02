package co.nilin.opex.matching.gateway.ports.postgres.dao

import co.nilin.opex.matching.gateway.ports.postgres.model.PairSettingModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PairSettingRepository : ReactiveCrudRepository<PairSettingModel, String> {
    fun findByPair(pair: String): Mono<PairSettingModel>
}
