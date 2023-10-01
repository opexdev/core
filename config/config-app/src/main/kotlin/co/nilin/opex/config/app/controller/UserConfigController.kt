package co.nilin.opex.config.app.controller

import co.nilin.opex.config.app.dto.UpdateUserConfigRequest
import co.nilin.opex.config.core.inout.UserWebConfig
import co.nilin.opex.config.core.spi.UserConfigManager
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user/v1")
class UserConfigController(private val userConfigManager: UserConfigManager) {

    @GetMapping("/{uuid}")
    fun getUserConfig(@PathVariable uuid: String): UserWebConfig {
        return userConfigManager.getUserConfig(uuid)
    }

    @PostMapping("/{uuid}")
    fun updateConfig(@PathVariable uuid: String, @RequestBody request: UpdateUserConfigRequest): UserWebConfig {
        request.apply {
            theme?.let { userConfigManager.updateThemeConfig(uuid, it) }
            language?.let { userConfigManager.updateLanguageConfig(uuid, it) }
            favoritePairs?.let { userConfigManager.updateFavoritePairsConfig(uuid, it) }
        }
        return userConfigManager.getUserConfig(uuid)
    }

    @PostMapping("/{uuid}/pair")
    fun addFavoritePair(@PathVariable uuid: String, @RequestBody pairs: Set<String>): UserWebConfig {
        return userConfigManager.addFavoritePair(uuid, pairs)
    }

    @DeleteMapping("/{uuid}/pair")
    fun removeFavoritePair(@PathVariable uuid: String, @RequestBody pairs: Set<String>): UserWebConfig {
        return userConfigManager.removeFavoritePair(uuid, pairs)
    }

}