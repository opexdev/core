package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyRepository : ReactiveCrudRepository<CurrencyModel, String> {

    @Query("select * from currency where symbol = :symbol")
    fun findBySymbol(symbol: String): Mono<CurrencyModel>

    @Query("insert into currency values (:name, :symbol, :precision) on conflict do nothing")
    fun insert(name: String, symbol: String, precision: Double): Mono<CurrencyModel>

    @Query("delete from currency where name = :name")
    fun deleteByName(name: String): Mono<Void>

}