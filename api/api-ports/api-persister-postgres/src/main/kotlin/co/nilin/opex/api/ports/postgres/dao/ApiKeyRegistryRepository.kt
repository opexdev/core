package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.ApiKeyRegistryModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApiKeyRegistryRepository : ReactiveCrudRepository<ApiKeyRegistryModel, String> {
}