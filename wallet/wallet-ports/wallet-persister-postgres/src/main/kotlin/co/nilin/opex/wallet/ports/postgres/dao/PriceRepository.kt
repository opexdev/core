package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.PriceModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface PriceRepository : ReactiveCrudRepository<PriceModel, String> {

    @Query("""
        insert into price(symbol, price, updated_date)
        values (:symbol, :price, now())
        on conflict (symbol)
        do update set price = excluded.price, updated_date = now()
    """)
    fun upsert(symbol: String, price: BigDecimal): Mono<Void>
}