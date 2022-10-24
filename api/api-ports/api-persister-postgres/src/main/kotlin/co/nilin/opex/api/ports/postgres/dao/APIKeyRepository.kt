package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.APIKey
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface APIKeyRepository : ReactiveCrudRepository<APIKey, Long> {
}