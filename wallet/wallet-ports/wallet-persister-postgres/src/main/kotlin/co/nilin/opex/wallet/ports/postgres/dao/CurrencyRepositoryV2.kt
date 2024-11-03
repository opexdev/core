package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.*

@Repository
interface CurrencyRepositoryV2 : ReactiveCrudRepository<CurrencyModel, String> {

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
    fun findByIsTransitive(isTransitive: Boolean): Flux<CurrencyModel>?

    @Query("select * from currency where (:symbol is null or symbol=:symbol ) and (:uuid is null or uuid=:uuid ) order by display_order ")
    fun fetchCurrency(uuid: String? = null, symbol: String? = null): Mono<CurrencyModel>?


    fun findBySymbol(symbol: String? = null): Mono<CurrencyModel>?

    @Query("select * from currency where  (:symbol is null  or symbol=:symbol) and (:name is null or name =:name  ) order by display_order")
    fun fetchSemiCurrencies( symbol: String? = null, name: String? = null): Flux<CurrencyModel>?


    @Query("insert into currency(symbol,uuid,name,precision,title,alias,icon,is_transitive,is_active,sign,description,short_description,external_url,display_order) values(:symbol,:uuid,:name,:precision,:title,:alias,:icon,:isTransitive,:isActive,:sign,:description,:shortDescription,:externalUrl,:order)  ")
    fun insert(symbol: String,
               uuid: String,
               name: String,
               precision: BigDecimal,
               title: String? = null,
               alias: String? = null,
               icon: String? = null,
               isTransitive: Boolean? = false,
               isActive: Boolean? = true,
               sign: String? = null,
               description: String? = null,
               shortDescription: String? = null,
               externalUrl: String? = null,
               order:Int?=null): Mono<Void>

}
