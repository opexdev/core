package co.nilin.opex.profile.ports.postgres.utils

object RegexPatterns {
    val EMAIL = Regex("""^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""")
    val MOBILE = Regex("""^09\d{9}$""")
    val IBAN = Regex("""^IR[0-9]{24}$""")
}