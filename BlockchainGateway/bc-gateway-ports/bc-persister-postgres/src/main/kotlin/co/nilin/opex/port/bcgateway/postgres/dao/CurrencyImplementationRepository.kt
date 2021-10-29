package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.CurrencyImplementationModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyImplementationRepository : ReactiveCrudRepository<CurrencyImplementationModel, Long> {

    fun findBySymbol(symbol: String): Flow<CurrencyImplementationModel>

    fun findByChain(chain: String): Flow<CurrencyImplementationModel>

    fun findByChainAndTokenAddress(chain: String, tokenAddress: String?): Mono<CurrencyImplementationModel>
}
