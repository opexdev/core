package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletType
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface WalletRepository : ReactiveCrudRepository<WalletModel, Long> {

    @Query("select * from wallet where owner = :owner and wallet_type = :type and currency = :currency ")
    fun findByOwnerAndTypeAndCurrency(
        @Param("owner") owner: Long,
        @Param("type") type: String,
        @Param("currency") currency: String
    ): Mono<WalletModel?>

    @Query("select * from wallet where owner = :owner and wallet_type = :type")
    fun findByOwnerAndType(
        @Param("owner") owner: Long,
        @Param("type") type: String,
    ): Flux<WalletModel>

    @Query("select * from wallet where owner = :owner")
    fun findByOwner(@Param("owner") owner: Long): Flux<WalletModel>

    @Query("select * from wallet where owner = :owner and currency = :currency")
    fun findByOwnerAndCurrency(owner: Long, currency: String): Flux<WalletModel>

    @Modifying
    @Query("update wallet set balance = balance + :balance, version = version + 1 where id = :id and version = :version")
    fun updateBalance(id: Long, delta: BigDecimal, version: Long): Mono<Int>

    @Modifying
    @Query("update wallet set balance = balance + :balance, version = version + 1 where id = :id")
    fun updateBalance(id: Long, delta: BigDecimal): Mono<Int>

    @Query("select * from wallet where owner = :ownerId and balance > 0")
    fun findAllAmountNotZero(ownerId: Long): Flux<WalletModel>

    @Query(
        """
        select wo.uuid, wo.title, w.wallet_type, w.currency, w.balance from wallet_owner wo
        join wallet w on w.owner = wo.id
        where (:walletType is NULL or wallet_type = :walletType)
            and (:currency is null or w.currency = :currency)
            and (:uuid is null or wo.uuid = :uuid)
        limit :limit offset :offset
    """
    )
    fun findWalletDataByCriteria(
        uuid: String?,
        walletType: String?,
        currency: String?,
        limit: Int,
        offset: Int
    ): Flux<WalletData>
}