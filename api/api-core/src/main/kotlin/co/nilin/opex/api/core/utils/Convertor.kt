package co.nilin.opex.api.core.utils

fun Set<String>?.toCsv(): String? = this?.joinToString(",")

fun String?.toSet(): Set<String>? = this
    ?.takeIf { it.isNotBlank() }
    ?.split(',')
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() }
    ?.toSet()
