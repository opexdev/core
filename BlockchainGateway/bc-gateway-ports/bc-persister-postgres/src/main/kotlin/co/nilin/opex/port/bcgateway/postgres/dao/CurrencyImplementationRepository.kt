package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.CurrencyImplementationModel
import co.nilin.opex.port.bcgateway.postgres.model.CurrencyModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyImplementationRepository : ReactiveCrudRepository<CurrencyImplementationRepository, Long> {
    @Query("select * from currency_implementations where chain = :chain and (:address is null or token_address = :address)")
    fun findByChainAndAddress(
        @Param("chain") chain: String,
        @Param("address") address: String?
    ): Mono<CurrencyImplementationModel>

    @Query("select * from currency_implementations where chain = :chain")
    fun findByChain(@Param("chain") chain: String): Flow<CurrencyImplementationModel>
}
