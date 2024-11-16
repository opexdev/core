package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.BankDataCommand
import co.nilin.opex.wallet.core.spi.BankDataManager
import co.nilin.opex.wallet.ports.postgres.dao.BankDataRepository
import co.nilin.opex.wallet.ports.postgres.model.BankDataModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.util.Collections

@Component
class BankDataManagerImpl(private val bankDataRepository: BankDataRepository) : BankDataManager {
    override suspend fun save(bankDataCommand: BankDataCommand):BankDataCommand? {
        bankDataRepository.findByIdentifier(bankDataCommand.identifier)?.awaitSingleOrNull()
            ?.let { throw OpexError.BankDataIsExist.exception() }
       return _save(bankDataCommand.toModel())?.toDto()
    }

    override suspend fun update(bankDataCommand: BankDataCommand): BankDataCommand? {
        _loadBankData(bankDataCommand.uuid!!)?.let {
            return _save(bankDataCommand.toModel().apply { id = it.id })?.toDto()
        } ?: throw OpexError.BankDataIsExist.exception()
    }

    override suspend fun delete(uuid: String) {
        _loadBankData(uuid)?.let {
            bankDataRepository.deleteById(it.id!!)?.awaitSingleOrNull()
        } ?: throw OpexError.BankDataNotFound.exception()
    }

    override suspend fun fetchBankData(): List<BankDataCommand>? {
        return bankDataRepository.findAll().map { it.toDto() }?.collectList()?.awaitSingleOrNull()
    }

    override suspend fun fetchBankData(uuid: String): BankDataCommand? {
        return _loadBankData(uuid!!)?.let {
            it.toDto()
        } ?: throw OpexError.BankDataIsExist.exception()
    }



    private suspend fun _save(bankDataModel: BankDataModel): BankDataModel? {
        return bankDataRepository.save(bankDataModel)?.awaitSingleOrNull()

    }

    private suspend fun _loadBankData(uuid: String): BankDataModel? {
        return bankDataRepository.findByUuid(uuid)?.awaitSingleOrNull()

    }
}