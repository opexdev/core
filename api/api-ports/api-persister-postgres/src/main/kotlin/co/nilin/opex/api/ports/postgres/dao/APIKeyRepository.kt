package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.APIKeyModel
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface APIKeyRepository : CrudRepository<APIKeyModel, Long> {

    fun findAllByUserId(userId: String): List<APIKeyModel>

    fun findByKey(key: String): APIKeyModel?

    fun countByUserId(userId: String): Long?

}