package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.KycLevel
import co.nilin.opex.accountant.core.spi.UserLevelLoader
import co.nilin.opex.accountant.ports.postgres.dao.UserLevelMapperRepository
import co.nilin.opex.accountant.ports.postgres.dao.UserLevelRepository
import co.nilin.opex.accountant.ports.postgres.model.UserLevelMapperModel
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class UserLevelLoaderImpl(
    private val userLevelMapperRepository: UserLevelMapperRepository,
    private val userLevelRepository: UserLevelRepository
) : UserLevelLoader {

    override suspend fun load(uuid: String): String {
        val mapper = userLevelMapperRepository.findByUuid(uuid).awaitSingleOrNull()
        return mapper?.userLevel ?: "*"
    }

    override suspend fun update(uuid: String, userLevel: KycLevel) {

        userLevelRepository.findByLevel(userLevel.name).awaitSingleOrNull()?.let {
            userLevelMapperRepository.findByUuid(uuid).awaitSingleOrNull()
                ?.let {
                    userLevelMapperRepository.save(UserLevelMapperModel(it.id, it.uuid, userLevel.name))
                        .awaitSingleOrNull()
                }
                ?: run {
                    userLevelMapperRepository.save(UserLevelMapperModel(null, uuid, userLevel.name)).awaitSingleOrNull()
                }
        } ?: run {
            userLevelRepository.insert(userLevel.name).awaitSingleOrNull()
            userLevelMapperRepository.findByUuid(uuid).awaitSingleOrNull()
                ?.let {
                    userLevelMapperRepository.save(UserLevelMapperModel(it.id, it.uuid, userLevel.name))
                        .awaitSingleOrNull()
                }
                ?: run {
                    userLevelMapperRepository.save(UserLevelMapperModel(null, uuid, userLevel.name)).awaitSingleOrNull()
                }

        }
    }
}