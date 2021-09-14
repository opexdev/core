package co.nilin.opex.port.bcgateway.postgres.dao

import co.nilin.opex.port.bcgateway.postgres.model.WalletSyncScheduleModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface WalletSyncScheduleRepository : ReactiveCrudRepository<WalletSyncScheduleModel, Long> {
    @Query("select * from wallet_sync_schedules where retryTime <= :time")
    fun findActiveSchedule(time: LocalDateTime): Mono<WalletSyncScheduleModel>
}
