package co.nilin.opex.wallet.core.spi

interface Notifier {

    suspend fun notify(message: String): Boolean
}
