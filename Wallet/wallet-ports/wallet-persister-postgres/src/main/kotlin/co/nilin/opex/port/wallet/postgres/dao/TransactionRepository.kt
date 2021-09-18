package co.nilin.opex.port.wallet.postgres.dao

import co.nilin.opex.port.wallet.postgres.dto.TransactionStat
import co.nilin.opex.port.wallet.postgres.model.CurrencyModel
import co.nilin.opex.port.wallet.postgres.model.CurrencyRateModel
import co.nilin.opex.port.wallet.postgres.model.TransactionModel
import kotlinx.coroutines.flow.Flow
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Repository
interface TransactionRepository: ReactiveCrudRepository<TransactionModel, Long> {
    @Query("SELECT count(1) cnt, COALESCE(sum(source_amount * crm.rate), 0) total" +
            " FROM transaction tm " +
            " join wallet wm on wm.id = tm.source_wallet " +
            " join currency_rate crm on wm.currency = crm.source_currency " +
            " WHERE wm.owner = :owner " +
            " and wm.wallet_type = :walletType " +
            " and crm.dest_currency = :currency " +
            " and tm.transaction_date >= :startDate " +
            " and tm.transaction_date <= :endDate")
    fun calculateWithdrawStatisticsBasedOnCurrency(@Param("owner") owner: Long
                            ,@Param("walletType") walletType: String
                            ,@Param("startDate") startDate: LocalDateTime
                            ,@Param("endDate") endDate: LocalDateTime
                            ,@Param("currency") currency: String): Mono<TransactionStat>
    @Query("SELECT count(1) cnt, COALESCE(sum(source_amount), 0) total " +
            " FROM TransactionModel tm " +
            " join WalletModel wm on wm.id = tm.sourceWallet " +
            " WHERE wm.owner = :owner " +
            " and wm.id = :id " +
            " and tm.transaction_date >= :startDate " +
            " and tm.transaction_date <= :endDate")
    fun calculateWithdrawStatistics(@Param("owner") owner: Long
                                    ,@Param("walletId") wallet: Long
                                    ,@Param("startDate") startDate: LocalDateTime
                                    ,@Param("endDate") endDate: LocalDateTime): Mono<TransactionStat>

    @Query("SELECT count(1) cnt, COALESCE(sum(dest_amount),0) total " +
            " FROM transaction tm " +
            " join wallet wm on wm.id = tm.dest_wallet " +
            " join currency_rate crm on wm.currency = crm.source_currency " +
            " WHERE wm.owner = :owner " +
            " and wm.wallet_type = :walletType " +
            " and crm.dest_currency = :currency " +
            " and tm.transaction_date >= :startDate " +
            " and tm.transaction_date <= :endDate")
    fun calculateDepositStatisticsBasedOnCurrency(@Param("owner") owner: Long
                                                  ,@Param("walletType") walletType: String
                                                  ,@Param("startDate") startDate: LocalDateTime
                                                  ,@Param("endDate") endDate: LocalDateTime
                                                  ,@Param("currency") currency: String): Mono<TransactionStat>
    @Query("SELECT count(1) cnt, COALESCE(sum(dest_amount * crm.rate), 0) total" +
            " FROM transaction tm " +
            " join wallet wm on wm.id = tm.dest_wallet " +
            " WHERE wm.owner = :owner " +
            " and wm.id = :walletId " +
            " and tm.transaction_date >= :startDate " +
            " and tm.transaction_date <= :endDate")
    fun calculateDepositStatistics(@Param("owner") owner: Long
                                   ,@Param("walletId") wallet: Long
                                   ,@Param("startDate") startDate: LocalDateTime
                                   ,@Param("endDate") endDate: LocalDateTime): Mono<TransactionStat>

}