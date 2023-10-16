package co.nilin.opex.config.ports.redis.impl

import co.nilin.opex.config.core.inout.UserWebConfig
import co.nilin.opex.config.core.spi.UserConfigManager
import co.nilin.opex.config.ports.redis.dao.UserWebConfigRepository
import co.nilin.opex.config.ports.redis.document.UserWebConfigDocument
import co.nilin.opex.config.ports.redis.utils.asDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserConfigManagerImpl(private val userWebConfigRepository: UserWebConfigRepository) : UserConfigManager {

    private val logger = LoggerFactory.getLogger(UserConfigManagerImpl::class.java)

    override fun saveNewUserWebConfig(
        uuid: String,
        theme: String,
        language: String,
        favPairs: Set<String>
    ): UserWebConfig {
        return userWebConfigRepository.save(UserWebConfigDocument(uuid, theme, language, favPairs.toHashSet())).asDTO()
    }

    override fun updateThemeConfig(uuid: String, theme: String): UserWebConfig {
        val config = getOrCreateConfig(uuid).apply { this.theme = theme }
        return userWebConfigRepository.save(config).asDTO()
    }

    override fun updateLanguageConfig(uuid: String, language: String): UserWebConfig {
        val config = getOrCreateConfig(uuid).apply { this.language = language }
        return userWebConfigRepository.save(config).asDTO()
    }

    override fun updateFavoritePairsConfig(uuid: String, pairs: Set<String>): UserWebConfig {
        val config = getOrCreateConfig(uuid).apply {
            favoritePairs.clear()
            favoritePairs.addAll(pairs)
        }
        return userWebConfigRepository.save(config).asDTO()
    }

    override fun addFavoritePair(uuid: String, pairs: Set<String>): UserWebConfig {
        val config = getOrCreateConfig(uuid).apply { favoritePairs.addAll(pairs) }
        return userWebConfigRepository.save(config).asDTO()
    }

    override fun removeFavoritePair(uuid: String, pairs: Set<String>): UserWebConfig {
        val config = getOrCreateConfig(uuid).apply { favoritePairs.removeAll(pairs) }
        return userWebConfigRepository.save(config).asDTO()
    }

    override fun getUserConfig(uuid: String): UserWebConfig {
        return getOrCreateConfig(uuid).asDTO()
    }

    private fun getOrCreateConfig(uuid: String): UserWebConfigDocument {
        return userWebConfigRepository.findById(uuid)
            .orElseGet {
                logger.info("Unable to find config for user $uuid. Creating new config object.")
                userWebConfigRepository.save(UserWebConfigDocument.default(uuid))
            }
    }
}