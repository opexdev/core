package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.SymbolMapModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SymbolMapRepository : ReactiveCrudRepository<SymbolMapModel, String> {

    @Query("select * from symbol_maps where symbol = :symbol and alias_key = :aliasKey")
    fun findByAliasKeyAndSymbol(aliasKey: String, @Param("symbol") symbol: String): Mono<SymbolMapModel>?

    @Query("select * from symbol_maps where alias_key = :aliasKey and alias = :alias")
    fun findByAliasKeyAndAlias(aliasKey: String, @Param("alias") alias: String): Mono<SymbolMapModel>?

    fun findAllByAliasKey(aliasKey: String): Flux<SymbolMapModel>
}
