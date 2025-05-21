package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController {

    val logger by LoggerDelegate()

    @GetMapping("/1")
    //@PreAuthorize("hasAuthority('PERM_order:write')")
    suspend fun test(@CurrentSecurityContext context: SecurityContext) {
        logger.info("1")
    }
}