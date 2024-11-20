package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.BankDataCommand

interface BankDataManager {

    suspend fun save(bankDataCommand: BankDataCommand): BankDataCommand?
    suspend fun update(bankDataCommand: BankDataCommand): BankDataCommand?
    suspend fun delete(uuid: String)
    suspend fun fetchBankData(): List<BankDataCommand>?
    suspend fun fetchBankData(uuid: String): BankDataCommand?
}