package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.CurrencyLocalizationModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CurrencyLocalizationsRepository : ReactiveCrudRepository<CurrencyLocalizationModel, Long> {
    suspend fun findByCurrency(currency: String): Flux<CurrencyLocalizationModel>

    @Modifying
    @Query(
        """
    INSERT INTO currency_localization (currency, name, title, alias, description, short_description, language)
    VALUES (:currency, :name, :title, :alias, :description, :shortDescription, :language)
    ON CONFLICT (currency, language)
    DO UPDATE SET
        name = :name,
        title = :title,
        alias = :alias,
        description = :description,
        short_description = :shortDescription
"""
    )
    fun upsert(
        currency: String,
        name: String?,
        title: String?,
        alias: String?,
        description: String?,
        shortDescription: String?,
        language: String
    ): Mono<Void>

}