package co.nilin.mixchange.port.accountant.postgres.dao

import co.nilin.mixchange.port.accountant.postgres.model.TempEventModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TempEventRepository: ReactiveCrudRepository<TempEventModel, Long> {
    fun findByOuid(ouid:String): Flow<TempEventModel>
    fun deleteByOuid(ouid:String): Mono<Void>
}