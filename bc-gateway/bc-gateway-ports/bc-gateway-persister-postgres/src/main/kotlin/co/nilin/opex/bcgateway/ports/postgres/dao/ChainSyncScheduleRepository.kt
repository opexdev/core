package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.ChainSyncScheduleModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface ChainSyncScheduleRepository : ReactiveCrudRepository<ChainSyncScheduleModel, String> {

    @Query("insert into chain_sync_schedules values (:chain, CURRENT_DATE, :delay, :errorDelay) on conflict do nothing")
    fun insert(chain: String, delay: Int, errorDelay: Int): Mono<Void>

    @Query("select * from chain_sync_schedules where retry_time <= :time")
    fun findActiveSchedule(time: LocalDateTime): Flow<ChainSyncScheduleModel>


}
