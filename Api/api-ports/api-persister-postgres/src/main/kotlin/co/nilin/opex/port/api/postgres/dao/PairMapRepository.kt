package co.nilin.opex.port.api.postgres.dao

import co.nilin.opex.port.api.postgres.model.PairMapModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PairMapRepository : ReactiveCrudRepository<PairMapModel, Long> {

    @Query("select * from pair_maps where pair=:pair")
    fun findByPair(@Param("pair") pair: String): Mono<PairMapModel>

    @Query("select * from pair_maps where map=:map")
    fun findByMap(@Param("map") map: String): Mono<PairMapModel>
}