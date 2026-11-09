package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.ActiveUserStat
import co.nilin.opex.api.core.inout.DailyLoginStat

interface DeviceManagementProxy {

    suspend fun getDailyLogins(token: String, fromMillis: Long, toMillis: Long): List<DailyLoginStat>

    suspend fun getActiveUsers(token: String, fromMillis: Long, toMillis: Long): ActiveUserStat
}
