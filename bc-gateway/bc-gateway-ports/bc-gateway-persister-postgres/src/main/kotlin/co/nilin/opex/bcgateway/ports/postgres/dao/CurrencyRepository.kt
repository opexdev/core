package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

//@Repository
//interface CurrencyRepository : ReactiveCrudRepository<CurrencyModel, String> {
//
////    fun findBySymbol(symbol: String): Mono<CurrencyModel>
////
////    @Query("insert into currency values (:symbol, :name) on conflict do nothing")
////    fun insert(name: String, symbol: String): Mono<CurrencyModel>
////
////    @Query("delete from currency where name = :name")
////    fun deleteByName(name: String): Mono<Void>
//}
