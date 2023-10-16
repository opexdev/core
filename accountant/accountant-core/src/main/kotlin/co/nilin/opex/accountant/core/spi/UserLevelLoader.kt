package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.KycLevel

interface UserLevelLoader {

    suspend fun load(uuid: String): String

    suspend fun update(uuid: String,userLevel:KycLevel)

}