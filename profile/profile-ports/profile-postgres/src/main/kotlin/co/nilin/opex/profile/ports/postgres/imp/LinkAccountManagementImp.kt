package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.kyc.core.utils.convert
import co.nilin.opex.profile.core.data.linkedbankAccount.*
import co.nilin.opex.profile.core.spi.LinkedAccountPersister
import co.nilin.opex.profile.ports.postgres.dao.LinkedAccountHistoryRepository
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
class LinkAccountManagementImp(val linkedAccountRepository: LinkedAccountRepository,
                               val profileRepository: ProfileRepository,
                               val linkedAccountHistoryRepository: LinkedAccountHistoryRepository) : LinkedAccountPersister {
    override suspend fun addNewAccount(linkedBankAccountRequest: LinkedBankAccountRequest): Mono<LinkedAccountResponse> {
        return profileRepository.findByUserId(linkedBankAccountRequest.userId!!)?.let {
            linkedAccountRepository.save(linkedBankAccountRequest.convert(LinkedBankAccountModel::class.java).apply {
                enabled = true
                verified = false
                accountId = UUID.randomUUID().toString()
                registerDate = LocalDateTime.now()
            }).doOnError { throw OpexException(OpexError.DuplicateAccount) }
                    .map { d -> d.convert(LinkedAccountResponse::class.java) }
        } ?: throw OpexException(OpexError.UserNotFound)
    }

    override suspend fun updateAccount(updateRelatedAccountRequest: UpdateRelatedAccountRequest): Mono<LinkedAccountResponse>? {
        return linkedAccountRepository.findAllByUserIdAndAccountId(updateRelatedAccountRequest.userId!!, updateRelatedAccountRequest.accountId!!)?.awaitFirstOrNull()
                ?.let { d ->
                    d.enabled = updateRelatedAccountRequest.status == Status.Enable
                    linkedAccountRepository.save(d)

                }?.map { d -> d.convert(LinkedAccountResponse::class.java) }
                ?: throw OpexException(OpexError.InvalidLinkedAccount)
    }

    override suspend fun getAccounts(userId: String): Flow<LinkedAccountResponse>? {
        return profileRepository.findByUserId(userId)?.awaitFirstOrNull()?.let {
            linkedAccountRepository.findAllByUserId(userId)?.map { d -> d.convert(LinkedAccountResponse::class.java) }
        } ?: throw OpexException(OpexError.UserNotFound)
    }

    override suspend fun getHistory(userId: String): Flow<LinkedAccountHistoryResponse>? {
          return  linkedAccountHistoryRepository.findAllByAccountId(userId)?.map { d -> d.convert(LinkedAccountHistoryResponse::class.java) }
    }

    override suspend fun verifyAccount(verifyRequest: VerifyLinkedAccountRequest): Mono<LinkedAccountResponse>? {
        return linkedAccountRepository.save(linkedAccountRepository.findByAccountId(verifyRequest.accountId!!)
                ?.awaitFirstOrNull()?.let { d ->
                    d.apply {
                        verified = verifyRequest.verified
                        verifier = verifyRequest.verifier
                        description = verifyRequest.description
                    }
                }
                ?: throw OpexException(OpexError.AccountNotFound))?.map { d -> d?.convert(LinkedAccountResponse::class.java) }
    }

    override suspend fun deleteAccount(deleteLinkedAccountRequest: DeleteLinkedAccountRequest) {
        linkedAccountRepository.findAllByUserIdAndAccountId(deleteLinkedAccountRequest.userId, deleteLinkedAccountRequest.accountId)?.awaitFirstOrNull()?.let {
            linkedAccountRepository.deleteByAccountIdAndUserId(deleteLinkedAccountRequest.accountId, deleteLinkedAccountRequest.userId)?.awaitFirstOrNull()
        } ?: throw OpexException(OpexError.AccountNotFound)
    }
}