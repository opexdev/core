package co.nilin.opex.price.core.spi

interface Notifier {
    suspend fun notify(message: String)
}
