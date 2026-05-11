package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UpdateWebConfigRequest
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

}