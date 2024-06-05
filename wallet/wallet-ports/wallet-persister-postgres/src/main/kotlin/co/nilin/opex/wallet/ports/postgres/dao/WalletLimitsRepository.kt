package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import kotlinx.coroutines.flow.Flow
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
        @Param("currency") currency: Long,
        @Param("action") action: String,
        @Param("walletType") walletType: String
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_Limits where owner = :owner and currency = :currency and action = :action and wallet_type = :walletType")
    fun findByOwnerAndCurrencyAndActionAndWalletType(
        @Param("owner") owner: Long,
        @Param("currency") currency: Long,
        @Param("action") action: String,
        @Param("walletType") walletType: String
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_limits where owner = :owner and currency = :currency and action = :action and wallet_id = :wallet")
    fun findByOwnerAndCurrencyAndWalletAndAction(
        @Param("owner") owner: Long,
        @Param("currency") currency: Long,
        @Param("wallet") wallet: Long,
        @Param("action") action: String
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_limits where level = :level and action = :action and owner is null")
    fun findByLevelAndAction(
        @Param("level") level: String,
        @Param("action") action: String
    ): Flow<WalletLimitsModel?>

    @Query("select * from wallet_limits where owner = :owner and action = :action")
    fun findByOwnerAndAction(
        @Param("owner") owner: Long,
        @Param("action") action: String
    ): Flow<WalletLimitsModel?>
}