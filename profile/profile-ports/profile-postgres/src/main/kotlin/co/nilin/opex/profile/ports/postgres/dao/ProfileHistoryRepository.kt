package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.history.ProfileHistory
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileHistoryRepository : ReactiveCrudRepository<ProfileHistory, Long> {
    fun findByUserId(userId: String, pageable: Pageable): Flow<ProfileHistory>
}