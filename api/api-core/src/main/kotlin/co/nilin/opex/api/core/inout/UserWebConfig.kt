package co.nilin.opex.api.core.inout

import co.nilin.opex.common.data.CalenderType
import co.nilin.opex.common.data.Theme
import co.nilin.opex.common.data.UserLanguage

data class UserWebConfig(
    var theme: Theme,
    var language: UserLanguage,
    var calender: CalenderType,
    val favoritePairs: Set<String> = hashSetOf()
)