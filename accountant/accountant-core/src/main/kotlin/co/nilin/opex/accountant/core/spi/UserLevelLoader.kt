package co.nilin.opex.accountant.core.spi

interface UserLevelLoader {

    suspend fun load(uuid: String): String

}