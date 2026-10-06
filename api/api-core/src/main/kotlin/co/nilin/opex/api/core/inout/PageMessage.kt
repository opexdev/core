package co.nilin.opex.api.core.inout

import co.nilin.opex.common.data.UserLanguage

enum class UiPage {
    MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET
}

enum class PageMessageType {
    DEFAULT, INFO, WARNING, ERROR, SUCCESS
}

data class PageMessage(
    val page: UiPage,
    var type: PageMessageType = PageMessageType.DEFAULT,
    var link: String? = null,
    var translations: Map<UserLanguage, String> = emptyMap()
)

data class LocalizedPageMessage(
    val page: UiPage,
    val type: PageMessageType,
    val link: String?,
    val text: String
)

data class UpdatePageMessageRequest(
    val page: UiPage,
    val type: PageMessageType?,
    val link: String?,
    val translations: Map<UserLanguage, String>
)
