package co.nilin.opex.price.ports.postgres.dao

import co.nilin.opex.price.ports.postgres.model.PairProviderIncludeModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PairProviderIncludeRepository : ReactiveCrudRepository<PairProviderIncludeModel, Int> {

    @Query("SELECT * FROM pair_provider_include WHERE symbol = :symbol")
    fun findBySymbol(symbol: String): Flux<PairProviderIncludeModel>

    @Query("DELETE FROM pair_provider_include WHERE symbol = :symbol")
    fun deleteBySymbol(symbol: String): Mono<Void>
}
