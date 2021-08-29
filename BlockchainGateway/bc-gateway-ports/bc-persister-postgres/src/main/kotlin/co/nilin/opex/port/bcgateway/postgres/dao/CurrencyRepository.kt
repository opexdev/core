package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.CurrencyModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CurrencyRepository : ReactiveCrudRepository<CurrencyModel, Long> {
    @Query("select * from currency where symbol = :symbol")
    fun findBySymbol(@Param("symbol") symbol: String): Mono<CurrencyModel>

    @Query("select * from currency where name = :name")
    fun findByName(@Param("name") name: String): Mono<CurrencyModel>
}
