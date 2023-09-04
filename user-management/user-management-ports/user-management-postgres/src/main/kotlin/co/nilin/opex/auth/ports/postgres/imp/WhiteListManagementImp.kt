package co.nilin.opex.auth.ports.postgres.imp

import co.nilin.opex.auth.core.spi.WhiteListPersister
import co.nilin.opex.auth.ports.postgres.dao.TestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class WhiteListManagementImp(private val testRepository: TestRepository) : WhiteListPersister {
    override fun add(identifier: String) {
        TODO("Not yet implemented")
    }

    override fun delete(identifier: String) {
        TODO("Not yet implemented")
    }

    override fun getAll(): Flow<String>? {
        return testRepository.findAllBy()?.map { it.email ?: "" }
    }
}