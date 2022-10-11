package co.nilin.opex.auth.gateway.config

import co.nilin.opex.auth.gateway.data.Whitelist
import co.nilin.opex.utility.preferences.Preferences
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class WhitelistConfig(private val preferences: Preferences) {

    private val logger = LoggerFactory.getLogger(WhitelistConfig::class.java)

    @Bean("whitelist")
    fun whitelist(): Whitelist {
        val whitelist = with(preferences.auth.whitelist) {
            val file = File(file)
            if (!enabled) {
                logger.info("whitelist disabled by preferences")
                Whitelist()
            }

            if (!file.exists()) {
                logger.info("whitelist file doesn't exists")
                Whitelist()
            }

            val list = file.readLines().onEach { it.trim().toLowerCase() }
            Whitelist(list.isNotEmpty(), list)
        }

        logger.info("whitelist enabled: ${whitelist.isEnabled}")
        logger.info("whitelist emails: ${whitelist.emails}")
        return whitelist
    }

}