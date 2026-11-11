package co.nilin.opex.device.app.controller

import co.nilin.opex.device.core.data.ActiveUserStat
import co.nilin.opex.device.core.data.DailyLoginStat
import co.nilin.opex.device.core.service.LoginStatService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/stats")
class LoginStatAdminController(
    private val loginStatService: LoginStatService
) {

    @GetMapping("/daily-logins")
    suspend fun dailyLogins(
        @RequestParam("from") from: Long,
        @RequestParam("to") to: Long
    ): List<DailyLoginStat> {
        return loginStatService.dailyLogins(from, to)
    }

    @GetMapping("/active-users")
    suspend fun activeUsers(
        @RequestParam("from") from: Long,
        @RequestParam("to") to: Long
    ): ActiveUserStat {
        return loginStatService.activeUsers(from, to)
    }
}
