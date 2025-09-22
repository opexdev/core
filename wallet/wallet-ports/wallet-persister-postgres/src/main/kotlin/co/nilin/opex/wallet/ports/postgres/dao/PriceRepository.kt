package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.PriceModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface PriceRepository : ReactiveCrudRepository<PriceModel, String> {

    @Query(
        """
        insert into price(base_currency,quote_currency, price, update_date)
        values (:baseCurrency,:quoteCurrency ,:price, now())
        on conflict (base_currency,quote_currency)
        do update set price = excluded.price, update_date = now()
    """
    )
    fun upsert(baseCurrency: String, quoteCurrency: String, price: BigDecimal): Mono<Void>
}