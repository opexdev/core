package co.nilin.mixchange.port.wallet.postgres.dao

import co.nilin.mixchange.port.wallet.postgres.model.WalletLimitsModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WalletLimitsRepository : ReactiveCrudRepository<WalletLimitsModel, Long> {
    @Query("select * from wallet_limits where level = :level and currency = :currency and action = :action and wallet_type = :walletType")
    fun findByLevelAndCurrencyAndActionAndWalletType(
        @Param("level") level: String,
        @Param("currency") currency: String,
        @Param("action") action: String,
        @Param("walletType") walletType: String
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_Limits where owner = :owner and currency = :currency and action = :action and wallet_type = :walletType")
    fun findByOwnerAndCurrencyAndActionAndWalletType(
        @Param("owner") owner: Long,
        @Param("currency") currency: String,
        @Param("action") action: String,
        @Param("walletType") walletType: String
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_limits where owner = :owner and currency = :currency and action = :action and wallet_id = :wallet")
    fun findByOwnerAndCurrencyAndWalletAndAction(
        @Param("owner") owner: Long,
        @Param("currency") currency: String,
        @Param("wallet") wallet: Long,
        @Param("action") action: String
    ): Mono<WalletLimitsModel?>
}