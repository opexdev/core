package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.PairConfigModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PairConfigRepository : ReactiveCrudRepository<PairConfigModel, String> {

    @Query("insert into pair_config values (:pair, :leftSide, :rightSide, :leftSideFraction, :rightSideFraction) on conflict do nothing")
    fun insert(
        pair: String,
        leftSide: String,
        rightSide: String,
        leftSideFraction: Double,
        rightSideFraction: Double
    ): Mono<Void>
}
