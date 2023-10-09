package co.nilin.opex.config.app.controller

import co.nilin.opex.config.app.dto.UpdateUserConfigRequest
import co.nilin.opex.config.core.inout.UserWebConfig
import co.nilin.opex.config.core.spi.UserConfigManager
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/user/v1")
class UserConfigController(private val userConfigManager: UserConfigManager) {

    @GetMapping
    fun getUserConfig(principal: Principal): UserWebConfig {
        return userConfigManager.getUserConfig(principal.name)
    }

    @PostMapping
    fun updateConfig(principal: Principal, @RequestBody request: UpdateUserConfigRequest): UserWebConfig {
        request.apply {
            theme?.let { userConfigManager.updateThemeConfig(principal.name, it) }
            language?.let { userConfigManager.updateLanguageConfig(principal.name, it) }
            favoritePairs?.let { userConfigManager.updateFavoritePairsConfig(principal.name, it) }
        }
        return userConfigManager.getUserConfig(principal.name)
    }

    @PostMapping("/pair")
    fun addFavoritePair(principal: Principal, @RequestBody pairs: Set<String>): UserWebConfig {
        return userConfigManager.addFavoritePair(principal.name, pairs)
    }

    @DeleteMapping("/pair")
    fun removeFavoritePair(principal: Principal, @RequestBody pairs: Set<String>): UserWebConfig {
        return userConfigManager.removeFavoritePair(principal.name, pairs)
    }

}