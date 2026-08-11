package co.nilin.opex.matching.gateway.ports.postgres.dao

import co.nilin.opex.matching.gateway.ports.postgres.model.PairCategoryModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PairCategoryRepository : CoroutineCrudRepository<PairCategoryModel, String> {
    fun findByPair(pair: String): Flow<PairCategoryModel>

    fun deleteByPair(pair: String): Mono<Void>
}