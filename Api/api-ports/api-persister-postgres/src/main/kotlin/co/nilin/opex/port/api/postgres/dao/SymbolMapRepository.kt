package co.nilin.opex.port.api.postgres.dao

import co.nilin.opex.port.api.postgres.model.SymbolMapModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface SymbolMapRepository : ReactiveCrudRepository<SymbolMapModel, String> {

    @Query("select * from symbol_maps where symbol = :symbol")
    fun findBySymbol(@Param("symbol") symbol: String): Mono<SymbolMapModel>

    @Query("select * from symbol_maps where value = :value")
    fun findByValue(@Param("value") value: String): Mono<SymbolMapModel>
}