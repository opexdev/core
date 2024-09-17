package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.WalletLimitAction
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WalletLimitsRepository : ReactiveCrudRepository<WalletLimitsModel, Long> {

    @Query("select * from wallet_limits where level = :level and currency = :currency and action = :action and wallet_type = :walletType")
    fun findByLevelAndCurrencyAndActionAndWalletType(
        level: String,
        currency: String,
        action: WalletLimitAction,
        walletType: WalletType
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_Limits where owner = :owner and currency = :currency and action = :action and wallet_type = :walletType")
    fun findByOwnerAndCurrencyAndActionAndWalletType(
        owner: Long,
        currency: String,
        action: WalletLimitAction,
        walletType: WalletType
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_limits where owner = :owner and currency = :currency and action = :action and wallet_id = :wallet")
    fun findByOwnerAndCurrencyAndWalletAndAction(
        owner: Long,
        currency: String,
        wallet: Long,
        action: WalletLimitAction
    ): Mono<WalletLimitsModel?>

    @Query("select * from wallet_limits where level = :level and action = :action and owner is null")
    fun findByLevelAndAction(level: String, action: WalletLimitAction): Flow<WalletLimitsModel?>

    @Query("select * from wallet_limits where owner = :owner and action = :action")
    fun findByOwnerAndAction(owner: Long, action: WalletLimitAction): Flow<WalletLimitsModel?>
}