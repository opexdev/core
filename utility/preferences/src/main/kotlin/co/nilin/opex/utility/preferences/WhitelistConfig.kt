package co.nilin.opex.utility.preferences

data class WhitelistConfig(
    val enabled: Boolean = false,
    val file: String = "/whitelist.txt"
)