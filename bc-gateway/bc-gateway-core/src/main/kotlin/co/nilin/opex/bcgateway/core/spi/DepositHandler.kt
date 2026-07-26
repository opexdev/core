package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.Deposit

interface DepositHandler {

    suspend fun findDepositsByHash(hash: List<String>): List<Deposit>

    suspend fun saveAll(deposits: List<Deposit>)

    suspend fun save(deposit: Deposit)
}