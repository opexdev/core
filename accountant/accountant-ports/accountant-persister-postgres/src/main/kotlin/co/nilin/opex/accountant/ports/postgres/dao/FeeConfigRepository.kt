package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.core.model.FeeConfig
import co.nilin.opex.accountant.core.model.UserFee
import co.nilin.opex.accountant.ports.postgres.model.FeeConfigModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface FeeConfigRepository : ReactiveCrudRepository<FeeConfigModel, Long> {

    @Query("select * from fee_config order by display_order")
    fun findAllByOrder(): Flux<FeeConfig>

    fun findByName(name: String): Mono<FeeConfig>

    @Query(
        """
    SELECT name,display_order,maker_fee,taker_fee
    FROM fee_config
    WHERE
        (
            condition = 'AND'
            AND (:assetVolume BETWEEN min_asset_volume AND COALESCE(max_asset_volume, :assetVolume))
            AND (:tradeVolume BETWEEN min_trade_volume AND COALESCE(max_trade_volume, :tradeVolume))
        )
        OR
        (
            condition = 'OR'
            AND (
                (:assetVolume BETWEEN min_asset_volume AND COALESCE(max_asset_volume, :assetVolume))
                OR (:tradeVolume BETWEEN min_trade_volume AND COALESCE(max_trade_volume, :tradeVolume))
            )
        )
    ORDER BY display_order DESC
    LIMIT 1
    """
    )
    fun findMatchingConfig(assetVolume: BigDecimal, tradeVolume: BigDecimal): Mono<UserFee>

}