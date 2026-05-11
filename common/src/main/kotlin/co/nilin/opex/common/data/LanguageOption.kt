package co.nilin.opex.common.data

data class LanguageOption(
    val label: String,
    val nativeLabel: String,
    val direction: LanguageDirection,
    val defaultCalender: CalenderType,
)