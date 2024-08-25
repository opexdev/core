package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyImplementationRepository : ReactiveCrudRepository<CurrencyOnChainGatewayModel, Long> {

    fun findByCurrencySymbol(currencySymbol: String): Flow<CurrencyOnChainGatewayModel>

    fun findByChain(chain: String): Flow<CurrencyOnChainGatewayModel>

    fun findByCurrencySymbolAndChain(currencySymbol: String, chain: String): Mono<CurrencyOnChainGatewayModel>

    fun findByChainAndTokenAddress(chain: String, tokenAddress: String?): Mono<CurrencyOnChainGatewayModel>
}
