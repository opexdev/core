package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.BankAccount
import co.nilin.opex.profile.core.spi.BankAccountPersister
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.profile.ports.postgres.dao.BankAccountRepository
import co.nilin.opex.profile.ports.postgres.model.entity.BankAccountModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class BankAccountManagementImp(
    private val bankAccountRepository: BankAccountRepository,
) : BankAccountPersister {

    override suspend fun save(bankAccount: BankAccount): BankAccount {
        val savedBankAccount =
            bankAccountRepository.save(bankAccount.convert(BankAccountModel::class.java)).awaitFirstOrNull()
                ?: throw OpexError.BadRequest.exception("Failed to save bank account")
        return savedBankAccount.convert(BankAccount::class.java)
    }

    override suspend fun findAll(uuid: String): List<BankAccount> {
        return bankAccountRepository.findAllByUuid(uuid).collectList().awaitFirstOrElse { emptyList() }
    }

    override suspend fun findAll(
        cardNumber: String?,
        iban: String?
    ): List<BankAccount> {
        return bankAccountRepository.findByCardNumberOrIban(cardNumber, iban).collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun findAll(
        uuid: String,
        cardNumber: String?,
        iban: String?
    ): List<BankAccount> {
        return bankAccountRepository.findByUuidAndCardNumberOrIban(uuid, cardNumber, iban).collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun delete(id: Long, uuid: String) {
        val bankAccount =
            bankAccountRepository.findById(id).awaitFirstOrNull() ?: throw OpexError.BankAccountNotFound.exception()
        if (bankAccount.uuid == uuid)
            bankAccountRepository.deleteById(id).awaitFirstOrNull()
        else throw OpexError.Forbidden.exception()
    }
}