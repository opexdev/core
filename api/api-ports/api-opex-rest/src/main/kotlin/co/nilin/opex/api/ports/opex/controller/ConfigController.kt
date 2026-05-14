package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UpdateUserConfigRequest
import co.nilin.opex.api.core.inout.UpdateWebConfigRequest
import co.nilin.opex.api.core.inout.UserLevelConfig
import co.nilin.opex.api.core.inout.UserWebConfig
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.common.data.WebConfig
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1")
class ConfigController(private val configProxy: ConfigProxy) {

    @GetMapping("/web/config")
    suspend fun getWebConfig(): WebConfig {
        return configProxy.getWebConfig()
    }

    @PutMapping("/admin/web/config")
    suspend fun updateWebConfig(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UpdateWebConfigRequest
    ): WebConfig {
        return configProxy.updateWebConfig(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping("/user-level/config")
    suspend fun getUserLevelConfig(): List<UserLevelConfig> {
        return configProxy.getUserLevelConfig()
    }

    @PutMapping("/admin/user-level/config")
    suspend fun updateUserLevelConfig(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody userLevelConfig: UserLevelConfig
    ): UserLevelConfig {
        return configProxy.updateUserLevelConfig(securityContext.jwtAuthentication().tokenValue(), userLevelConfig)
    }

    @DeleteMapping("/admin/user-level/config/{userLevel}/{language}")
    suspend fun updateUserLevelConfig(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable userLevel: String,
        @PathVariable language: String
    ) {
        configProxy.deleteUserLevelConfig(securityContext.jwtAuthentication().tokenValue(), userLevel, language)
    }

    @GetMapping("/user/config")
    suspend fun getUserConfig(@CurrentSecurityContext securityContext: SecurityContext): UserWebConfig {
        return configProxy.getUserConfig(securityContext.jwtAuthentication().tokenValue())
    }

    @PutMapping("/user/config")
    suspend fun updateConfig(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UpdateUserConfigRequest
    ): UserWebConfig {
        return configProxy.updateUserConfig(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping("/user/config/pair")
    suspend fun getUserFavoritePair(@CurrentSecurityContext securityContext: SecurityContext): Set<String> {
        return configProxy.getUserFavoritePair(securityContext.jwtAuthentication().tokenValue())
    }

    @PostMapping("/user/config/pair/{pair}")
    suspend fun addUserFavoritePair(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable pair: String
    ): Set<String> {
        return configProxy.addUserFavoritePair(securityContext.jwtAuthentication().tokenValue(), pair)
    }

    @DeleteMapping("/user/config/pair/{pair}")
    suspend fun removeUserFavoritePair(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable pair: String
    ): Set<String> {
        return configProxy.removeUserFavoritePair(securityContext.jwtAuthentication().tokenValue(), pair)
    }
}