package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.BankAccount

interface BankAccountPersister {
    suspend fun save(bankAccount: BankAccount)
    suspend fun findAll(uuid: String): List<BankAccount>
    suspend fun findAll(cardNumber: String?, iban: String?): List<BankAccount>
    suspend fun findAll(uuid: String, cardNumber: String?, iban: String?): List<BankAccount>
    suspend fun delete(id: Long, uuid: String)
}