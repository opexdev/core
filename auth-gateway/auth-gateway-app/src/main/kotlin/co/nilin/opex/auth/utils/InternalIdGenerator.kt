package co.nilin.opex.auth.utils

import kotlin.random.Random

fun generateRandomID(): String {
    val digits = IntArray(6) { Random.nextInt(0, 10) }
    val sum = digits.sum()
    val checksum = sum.toString().padStart(2, '0')
    return buildString(8) {
        digits.forEach { append(it) }
        append(checksum)
    }
}
