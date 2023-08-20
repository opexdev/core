package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.kyc.core.utils.convert
import co.nilin.opex.profile.core.data.linkedbankAccount.*
import co.nilin.opex.profile.core.spi.LinkedAccountPersister
import co.nilin.opex.profile.ports.postgres.dao.LinkedAccountRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.model.base.LinkedBankAccount
import co.nilin.opex.profile.ports.postgres.model.entity.LinkedBankAccountModel
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Component
class LinkAccountManagementImp(val linkedAccountRepository: LinkedAccountRepository, val profileRepository: ProfileRepository) : LinkedAccountPersister {
    override suspend fun addNewAccount(linkedBankAccountRequest: LinkedBankAccountRequest): Mono<LinkedAccountResponse> {
        return profileRepository.findByUserId(linkedBankAccountRequest.userId!!)?.let {
            linkedAccountRepository.save(linkedBankAccountRequest.convert(LinkedBankAccountModel::class.java).apply {
                enable = false
                verify = false
                accountId = UUID.randomUUID().toString()
                registerDate = LocalDateTime.now()
            }).doOnError { throw OpexException(OpexError.Error) }
                    .map { d -> d.convert(LinkedAccountResponse::class.java) }
        } ?: throw OpexException(OpexError.UserNotFound)
    }

    override suspend fun updateAccount(updateRelatedAccountRequest: UpdateRelatedAccountRequest): Mono<LinkedAccountResponse>? {
        return linkedAccountRepository.findAllByUserIdAndAccountId(updateRelatedAccountRequest.userId!!, updateRelatedAccountRequest.accountId!!)?.awaitFirstOrNull()
                ?.let { d ->
                    d.enable = updateRelatedAccountRequest.status == Status.Enable
                    linkedAccountRepository.save(d)

                }?.map { d -> d.convert(LinkedAccountResponse::class.java) } ?: throw OpexException(OpexError.Error)
    }

    override suspend fun getAccounts(userId: String): Flow<LinkedAccountResponse>? {
        return profileRepository.findByUserId(userId).let {
            linkedAccountRepository.findAllByUserId(userId)?.map { d -> d.convert(LinkedAccountResponse::class.java) }
        } ?: throw OpexException(OpexError.UserNotFound)
    }

    override suspend fun verifyAccount(verifyRequest: VerifyLinkedAccountRequest): Mono<LinkedAccountResponse>? {
        return linkedAccountRepository.save(linkedAccountRepository.findByAccountId(verifyRequest.accountId!!)
                ?.awaitFirstOrNull()?.let { d ->
                    d.apply {
                        verify = verifyRequest.verify
                        description = verifyRequest.description
                    }
                }
                ?: throw OpexException(OpexError.Error))?.map { d -> d?.convert(LinkedAccountResponse::class.java) }
    }

    override suspend fun deleteAccount(accountId: String) {
        linkedAccountRepository.deleteByAccountId(accountId)?.awaitFirstOrNull()
    }
}