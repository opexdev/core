package co.nilin.opex.accountant.core.spi

interface JsonMapper {
    fun serialize(input: Any): String?
    fun <T> deserialize(input: String, t: Class<T>): T
    fun toMap(input: Any): Map<String, Any>
}