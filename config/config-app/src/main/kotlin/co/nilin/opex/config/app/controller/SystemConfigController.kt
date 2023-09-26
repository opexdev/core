package co.nilin.opex.config.app.controller

import co.nilin.opex.config.app.dto.UpdateSystemConfigRequest
import co.nilin.opex.config.core.inout.SystemConfig
import co.nilin.opex.config.core.spi.SystemConfigManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/system/v1")
class SystemConfigController(private val systemConfigManager: SystemConfigManager) {

    @GetMapping
    fun getConfig(): SystemConfig {
        return systemConfigManager.getConfig()
    }

    @PostMapping
    fun updateConfig(@RequestBody request: UpdateSystemConfigRequest): SystemConfig {
        return systemConfigManager.updateConfig(
            SystemConfig(
                request.logoUrl,
                request.title,
                request.description,
                request.defaultLanguage,
                request.supportedLanguages,
                request.defaultTheme,
                request.supportEmail,
                request.baseCurrency,
                request.dateType
            )
        )
    }

}