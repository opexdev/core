package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.CurrencyPrecision
import co.nilin.opex.wallet.ports.postgres.dto.CurrencyView
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface CurrencyRepositoryV2 : ReactiveCrudRepository<CurrencyModel, String> {

    fun findByIsTransitive(isTransitive: Boolean): Flux<CurrencyModel>?

    @Query(
        """
    SELECT 
        c.symbol,
        c.uuid,
        cl.name,
        c.precision,
        cl.title,
        cl.alias,
        c.icon,
        c.is_transitive,
        c.is_active,
        c.sign,
        cl.description,
        cl.short_description,
        c.external_url ,
        c.display_order,
        c.max_order
    FROM currency c
    LEFT JOIN currency_localization cl 
        ON cl.currency = c.symbol AND cl.language = :lang
    WHERE (:symbol IS NULL OR c.symbol = :symbol)
      AND (:uuid IS NULL OR c.uuid = :uuid)
    ORDER BY c.display_order
"""
    )
    fun fetchCurrency(
        uuid: String? = null,
        symbol: String? = null,
        lang: String? = null
    ): Mono<CurrencyView>?


    @Query(
        """
    SELECT 
        c.symbol,
        c.uuid,
        cl.name,
        c.precision,
        cl.title,
        cl.alias,
        c.icon,
        c.is_transitive,
        c.is_active,
        c.sign,
        cl.description,
        cl.short_description,
        c.external_url,
        c.display_order,
        c.max_order
    FROM currency c
    LEFT JOIN currency_localization cl 
        ON cl.currency = c.symbol AND cl.language = :lang
    WHERE (:symbol IS NULL OR c.symbol = :symbol)
      AND (:name IS NULL OR cl.name = :name)
    ORDER BY c.display_order
"""
    )
    fun fetchSemiCurrencies(
        symbol: String? = null,
        name: String? = null,
        lang: String? = null
    ): Flux<CurrencyView>?


    @Query("insert into currency(symbol,uuid,precision,icon,is_transitive,is_active,sign,external_url,display_order) values(:symbol,:uuid,:precision,:icon,:isTransitive,:isActive,:sign,:externalUrl,:displayOrder)  ")
    fun insert(
        symbol: String,
        uuid: String,
        precision: BigDecimal,
        icon: String? = null,
        isTransitive: Boolean? = false,
        isActive: Boolean? = true,
        sign: String? = null,
        externalUrl: String? = null,
        displayOrder: Int? = null
    ): Mono<Void>


    @Query(
        """
    SELECT 
        c.symbol,
        c.uuid,
        cl.name,
        c.precision,
        cl.title,
        cl.alias,
        c.icon,
        c.is_transitive,
        c.is_active,
        c.sign,
        cl.description,
        cl.short_description,
        c.external_url,
        c.display_order,
        c.max_order
    FROM currency c
    LEFT JOIN currency_localization cl 
        ON cl.currency = c.symbol AND cl.language = :lang
    ORDER BY c.display_order
"""
    )
    fun fetchAll(lang: String): Flux<CurrencyView>

    @Query("select symbol,precision from currency")
    fun fetchAllCurrenciesPrecision(): Flux<CurrencyPrecision>

    @Query("select max_order from currency where symbol=:symbol")
    fun fetchCurrencyMaxOrder(symbol: String): Mono<BigDecimal>?
}
