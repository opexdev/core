package co.nilin.opex.api.core.inout

import co.nilin.opex.common.data.CalenderType
import co.nilin.opex.common.data.Theme
import co.nilin.opex.common.data.UserLanguage

data class UpdateUserConfigRequest(
    val theme: Theme?,
    val language: UserLanguage?,
    val calender: CalenderType?,
    val favoritePairs: Set<String>?
)