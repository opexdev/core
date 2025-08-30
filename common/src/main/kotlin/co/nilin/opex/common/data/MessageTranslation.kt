package co.nilin.opex.common.data

import co.nilin.opex.utility.error.data.UserLanguage

data class MessageTranslation(
    val key: String,
    var message: String? = "",
    var language: UserLanguage,
    var messageCategory: MessageCategory?=MessageCategory.DEFAULT
    )
