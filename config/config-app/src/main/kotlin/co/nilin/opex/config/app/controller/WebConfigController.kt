package co.nilin.opex.config.app.controller

import co.nilin.opex.config.app.dto.UpdateWebConfigRequest
import co.nilin.opex.config.core.inout.WebConfig
import co.nilin.opex.config.core.spi.WebConfigManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/web/v1")
class WebConfigController(private val webConfigManager: WebConfigManager) {

    @GetMapping
    fun getConfig(): WebConfig {
        return webConfigManager.getConfig()
    }

    @PostMapping
    fun updateConfig(@RequestBody request: UpdateWebConfigRequest): WebConfig {
        return webConfigManager.updateConfig(
            WebConfig(
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