package co.nilin.opex.auth.gateway.config

import co.nilin.opex.auth.gateway.data.Whitelist
import co.nilin.opex.utility.preferences.Preferences
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class WhitelistConfig(private val preferences: Preferences) {

    @Bean("whitelist")
    fun whitelist(): Whitelist {
        val whitelist = preferences.auth.whitelist
        val file = File(whitelist.file)
        if (!whitelist.enabled || !file.exists()) return Whitelist()
        val list = file.readLines().onEach { it.trim().toLowerCase() }
        return Whitelist(list.isNotEmpty(), list)
    }

}