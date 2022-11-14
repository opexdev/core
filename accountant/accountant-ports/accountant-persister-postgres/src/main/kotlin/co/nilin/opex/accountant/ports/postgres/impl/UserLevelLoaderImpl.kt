package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.spi.UserLevelLoader
import co.nilin.opex.accountant.ports.postgres.dao.UserLevelMapperRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class UserLevelLoaderImpl(private val userLevelMapperRepository: UserLevelMapperRepository) : UserLevelLoader {

    override suspend fun load(uuid: String): String {
        val mapper = userLevelMapperRepository.findByUuid(uuid).awaitSingleOrNull()
        return mapper?.userLevel ?: "*"
    }
}