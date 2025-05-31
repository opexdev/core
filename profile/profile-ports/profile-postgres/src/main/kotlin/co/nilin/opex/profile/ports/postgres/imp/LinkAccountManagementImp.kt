package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.linkedbankAccount.*
import co.nilin.opex.profile.ports.postgres.utils.convert
import co.nilin.opex.profile.core.spi.LinkedAccountPersister
import co.nilin.opex.profile.ports.postgres.dao.LinkedAccountHistoryRepository
import co.nilin.opex.profile.ports.postgres.dao.LinkedAccountRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.model.entity.LinkedBankAccountModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Component
class LinkAccountManagementImp(
    val linkedAccountRepository: LinkedAccountRepository,
    val profileRepository: ProfileRepository,
    val linkedAccountHistoryRepository: LinkedAccountHistoryRepository
) : LinkedAccountPersister {
    private val logger = LoggerFactory.getLogger(LinkAccountManagementImp::class.java)

    override suspend fun addNewAccount(linkedBankAccountRequest: LinkedBankAccountRequest): Mono<LinkedAccountResponse> {
        return profileRepository.findByUserId(linkedBankAccountRequest.userId!!)?.let {
            linkedAccountRepository.save(linkedBankAccountRequest.convert(LinkedBankAccountModel::class.java).apply {
                enabled = true
                verified = false
                accountId = UUID.randomUUID().toString()
                registerDate = LocalDateTime.now()
            }).doOnError { throw OpexError.DuplicateAccount.exception() }
                .map { d -> d.convert(LinkedAccountResponse::class.java) }
        } ?: throw OpexError.UserNotFound.exception()
    }

    override suspend fun updateAccount(updateRelatedAccountRequest: UpdateRelatedAccountRequest): Mono<LinkedAccountResponse>? {
        return linkedAccountRepository.findAllByUserIdAndAccountId(
            updateRelatedAccountRequest.userId!!,
            updateRelatedAccountRequest.accountId!!
        )?.awaitFirstOrNull()
            ?.let { d ->
                d.enabled = updateRelatedAccountRequest.status == Status.Enable
                linkedAccountRepository.save(d)

            }?.map { d -> d.convert(LinkedAccountResponse::class.java) }
            ?: throw OpexError.InvalidLinkedAccount.exception()
    }

    override suspend fun getOwner(accountNumber: String, partialSearch: Boolean?): Flow<LinkedAccountResponse>? {
        if (partialSearch == false) {
            logger.info("==========================$accountNumber")
            return linkedAccountRepository.findAllByNumber(accountNumber)
                ?.map { d -> d.convert(LinkedAccountResponse::class.java) }

        } else {
            logger.info("==========------------==========$accountNumber")

            return linkedAccountRepository.searchAllByNumber(accountNumber)
                ?.map { d -> d.convert(LinkedAccountResponse::class.java) }
        }
    }

    override suspend fun getAccounts(userId: String): Flow<LinkedAccountResponse>? {
        return profileRepository.findByUserId(userId)?.awaitFirstOrNull()?.let {
            linkedAccountRepository.findAllByUserId(userId)?.map { d -> d.convert(LinkedAccountResponse::class.java) }
        } ?: throw OpexError.UserNotFound.exception()
    }

    override suspend fun getHistory(userId: String): Flow<LinkedAccountHistoryResponse>? {
        return linkedAccountHistoryRepository.findAllByAccountId(userId)
            ?.map { d -> d.convert(LinkedAccountHistoryResponse::class.java) }
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
            ?: throw OpexError.AccountNotFound.exception())?.map { d -> d?.convert(LinkedAccountResponse::class.java) }
    }

    override suspend fun deleteAccount(deleteLinkedAccountRequest: DeleteLinkedAccountRequest): Mono<String>? {

        return linkedAccountRepository.findAllByUserIdAndAccountId(
            deleteLinkedAccountRequest.userId,
            deleteLinkedAccountRequest.accountId
        )?.awaitFirstOrNull()?.let {
            linkedAccountRepository.deleteByAccountIdAndUserId(
                deleteLinkedAccountRequest.accountId,
                deleteLinkedAccountRequest.userId
            ).awaitFirstOrNull().run { return Mono.just(deleteLinkedAccountRequest.accountId) }
        } ?: throw OpexError.InvalidLinkedAccount.exception()
    }
}