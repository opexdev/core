package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.NewCurrencyModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.security.cert.TrustAnchor
import java.time.LocalDateTime

@Repository
interface CurrencyRepositoryV2 : ReactiveCrudRepository<NewCurrencyModel, Long> {

//    @Query("select * from currency where symbol = :symbol")
//    fun findBySymbol(symbol: String): Mono<CurrencyModel>?
//
//    @Query("insert into currency values (:symbol, :name, :precision) on conflict do nothing")
//    fun insert(name: String, symbol: String, precision: BigDecimal): Mono<CurrencyModel>
//
//    @Query("delete from currency where name = :name")
//    fun deleteByName(name: String): Mono<Void>
//
//
//    fun deleteBySymbol(symbol: String): Mono<Void>
//
//
//    fun findByIsTransitive(isTransitive: Boolean): Flux<CurrencyModel>?

    @Query("select * from new_currency where (:uuid=null or :uuid=uuid) and (:symbol =null or symbol like CONCAT('%',:symbol,'%') ) and (:name =null or name like CONCAT('%',:name,'%') )  ")
    fun fetchCurrencies(uuid: String? = null, symbol: String? = null, name: String? = null): Flux<NewCurrencyModel>?


}
