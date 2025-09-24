package co.nilin.opex.common.data

//After adjusting versions in various modules it should be moved to common project

data class MessageTranslation(
    val key: String,
    var message: String? = "",
    var language: UserLanguage,
    var messageCategory: MessageCategory?=MessageCategory.DEFAULT
    )
