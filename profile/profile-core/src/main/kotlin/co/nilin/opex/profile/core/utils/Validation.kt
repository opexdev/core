package co.nilin.opex.profile.core.utils


import java.math.BigInteger
import java.util.Locale


private val IBAN_VALIDATION_DIVISOR = BigInteger("97")
private val IBAN_VALIDATION_REMAINDER = BigInteger("1")

/**
 * Check Bank card number validation
 *
 * @return return true if it is valid card number otherwise false
 */
fun CharSequence?.isValidCardNumber(): Boolean {
    this ?: return false
    if (!Regex("\\d{16}").matches(this))
        return false
    var sum = 0
    for (i in indices) sum += try {
        val character = get(i).toString().toInt()
        if (i % 2 == 0) {
            val temp = character * 2
            if (temp > 9) temp - 9 else temp
        } else character
    } catch (e: NumberFormatException) {
        return false
    }
    return sum % 10 == 0
}

/**
 * Check Bank IBAN number validation
 *
 * @return return true if it is valid IBAN number otherwise false
 */
fun CharSequence?.isValidIBAN(): Boolean {
    this ?: return false

    val iban = if (!toString().toUpperCase(Locale.ENGLISH).startsWith("IR")) {
        "IR$this"
    } else {
        this
    }
    if (iban.length != 26) return false
    if (!iban.startsWith("IR")) return false
    val cc = iban.substring(0, 2)
    val cd = iban.substring(2, 4)
    val bban = iban.substring(4, 26)
    val ccFirstCharValue = getCountryCodeValue(cc[0])
    val ccSecondCharValue = getCountryCodeValue(cc[1])
    val newIBAN = BigInteger(bban + ccFirstCharValue + ccSecondCharValue + cd)
    return newIBAN.mod(IBAN_VALIDATION_DIVISOR).compareTo(IBAN_VALIDATION_REMAINDER) == 0
}

private fun getCountryCodeValue(c: Char): Int {
    return c - 'A' + 10
}


