package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.AddBankAccountRequest
import co.nilin.opex.profile.app.dto.BankAccountResponse
import co.nilin.opex.profile.app.utils.toBankAccountResponse
import co.nilin.opex.profile.core.data.inquiry.IbanInfo
import co.nilin.opex.profile.core.data.profile.BankAccount
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequest
import co.nilin.opex.profile.core.data.profile.BankAccountStatus
import co.nilin.opex.profile.core.data.profile.NationalityType
import co.nilin.opex.profile.core.spi.BankAccountApprovalRequestPersister
import co.nilin.opex.profile.core.spi.BankAccountPersister
import co.nilin.opex.profile.core.spi.InquiryProxy
import co.nilin.opex.profile.core.spi.ProfilePersister
import co.nilin.opex.profile.core.utils.handleCardIbanInfoError
import co.nilin.opex.profile.core.utils.handleCardOwnershipError
import co.nilin.opex.profile.core.utils.handleIbanInfoError
import co.nilin.opex.profile.core.utils.handleIbanOwnershipError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BankAccountManagement(
    private val bankAccountPersister: BankAccountPersister,
    private val bankAccountApprovalRequestPersister: BankAccountApprovalRequestPersister,
    private val inquiryProxy: InquiryProxy,
    private val profilePersister: ProfilePersister,
    @Value("\${app.admin-approval.bank-account}")
    private var isAdminApprovalRequired: Boolean,
    @Value("\${app.inquiry.bank-account}")
    private var isAutomaticVerificationEnabled: Boolean,
) {

    private val logger = LoggerFactory.getLogger(BankAccountManagement::class.java)

    suspend fun addBankAccount(uuid: String, request: AddBankAccountRequest): BankAccountResponse {
        validateBankAccountOwnership(uuid, request.cardNumber, request.iban)
        val profile = profilePersister.getProfile(uuid)

        if (request.cardNumber.isNullOrBlank() && request.iban.isNullOrBlank())
            throw OpexError.InvalidRequestBody.exception("Either Card Number or IBAN must be provided")

        val identifier = profile.identifier
        val birthDate = profile.birthDate
        val canInquire =
            profile.nationality == NationalityType.IRANIAN && !identifier.isNullOrBlank() && birthDate != null

        val verified = when {
            !isAutomaticVerificationEnabled ->
                VerificationResult.NEEDS_REVIEW

            canInquire -> verifyWithInquiry(request, identifier!!, birthDate!!)

            isAdminApprovalRequired ->
                VerificationResult.NEEDS_REVIEW

            else -> throw OpexError.BankAccountInquiryNotApplicable.exception()
        }

        val bankAccount = BankAccount(
            uuid = uuid,
            name = request.name,
            cardNumber = request.cardNumber,
            iban = verified.iban ?: request.iban,
            accountNumber = verified.accountNumber,
            bank = verified.bank,
            status = if (verified.needsReview) BankAccountStatus.WAITING else BankAccountStatus.VERIFIED,
            createDate = LocalDateTime.now(),
            creator = "system"
        )

        val saved = bankAccountPersister.save(bankAccount)
        if (verified.needsReview) saveApprovalRequest(saved)
        return saved.toBankAccountResponse()
    }

    private suspend fun saveApprovalRequest(bankAccount: BankAccount) {
        bankAccountApprovalRequestPersister.save(
            BankAccountApprovalRequest(
                bankAccountId = bankAccount.id
                    ?: throw OpexError.BadRequest.exception("Saved bank account has no id"),
                userId = bankAccount.uuid,
                createDate = LocalDateTime.now()
            )
        )
    }

    suspend fun getBankAccounts(uuid: String): List<BankAccountResponse> {
        return bankAccountPersister.findAll(uuid)
            .map { it.toBankAccountResponse() }
    }

    suspend fun deleteBankAccount(id: Long, uuid: String) {
        bankAccountPersister.delete(id, uuid)
    }

    suspend fun isBankAccountOwnedByUser(uuid: String, cardNumber: String?, iban: String?): Boolean {
        return bankAccountPersister.findAll(uuid, cardNumber, iban).isNotEmpty()
    }

    suspend fun findAccountOwner(accountNumber: String): String? {
        val accounts = bankAccountPersister.findByAccountNumber(accountNumber)
        return accounts.firstOrNull { it.status == BankAccountStatus.VERIFIED }?.uuid
            ?: accounts.firstOrNull()?.uuid
    }

    /** Either the data the inquiries resolved, or the fact that an admin has to take over. */
    private data class VerificationResult(
        val iban: String? = null,
        val accountNumber: String? = null,
        val bank: String? = null,
        val needsReview: Boolean = false
    ) {
        companion object {
            val NEEDS_REVIEW = VerificationResult(needsReview = true)
        }
    }

    private suspend fun verifyWithInquiry(
        request: AddBankAccountRequest,
        nationalCode: String,
        birthDate: LocalDateTime
    ): VerificationResult {
        return try {
            if (!request.cardNumber.isNullOrBlank()) {
                verifyCardOwnership(request.cardNumber, nationalCode, birthDate)
                val ibanInfo = getIbanInfoByCardNumber(request.cardNumber)
                VerificationResult(ibanInfo.iban, ibanInfo.depositNumber, ibanInfo.bank)
            } else {
                verifyIbanOwnership(request.iban!!, nationalCode, birthDate)
                val ibanInfo = getIbanInfoByIban(request.iban)
                VerificationResult(request.iban, ibanInfo.depositNumber, ibanInfo.bank)
            }
        } catch (e: InquiryUnavailableException) {
            // The only reviewable outcome. Everything the provider actually answered — a mismatch,
            // an invalid card, a code we do not recognise — leaves this method as an OpexException
            // and stops the user right here
            if (!isAdminApprovalRequired) throw OpexError.InquiryServiceUnavailable.exception()
            logger.warn("Inquiry service unreachable, sending the account for admin review")
            VerificationResult.NEEDS_REVIEW
        }
    }

    /**
     * The inquiry service never answered. Kept separate from every [OpexException] an inquiry can
     * raise, because those all describe the user's data and must not become a review request.
     */
    private class InquiryUnavailableException(cause: Throwable) : RuntimeException(cause)

    /**
     * A read timeout, a refused connection, a 5xx or an unreadable response never reaches
     * [VerifyOwnershipResponse.isError]. That is the one case where nobody has judged the user's
     * data yet, so it is the one case an admin can pick up.
     */
    private suspend fun <T> callInquiry(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: OpexException) {
            // The proxy reports a server-side failure of the provider as unavailability. Anything
            // else it raises is an answer about the user's data and has to stop the user
            if (e.error == OpexError.InquiryServiceUnavailable) throw InquiryUnavailableException(e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logger.warn("Inquiry provider did not answer: ${e.javaClass.simpleName} ${e.message}")
            throw InquiryUnavailableException(e)
        }
    }

    private suspend fun verifyCardOwnership(
        cardNumber: String,
        nationalCode: String,
        birthDate: LocalDateTime
    ) {
        val ownershipResponse = callInquiry {
            inquiryProxy.verifyCardOwnership(cardNumber, nationalCode, birthDate)
        }
        if (ownershipResponse.isError()) handleCardOwnershipError(ownershipResponse.code)
        if (ownershipResponse.matched == false) throw OpexError.CardOwnershipMismatch.exception()
    }

    private suspend fun verifyIbanOwnership(
        iban: String,
        nationalCode: String,
        birthDate: LocalDateTime
    ) {
        val ownershipResponse = callInquiry {
            inquiryProxy.verifyIbanOwnership(iban, nationalCode, birthDate)
        }
        if (ownershipResponse.isError()) handleIbanOwnershipError(ownershipResponse.code)
        if (ownershipResponse.matched == false) throw OpexError.IbanOwnershipMismatch.exception()
    }

    private suspend fun getIbanInfoByCardNumber(cardNumber: String): IbanInfo {
        val ibanInfoResponse = callInquiry {
            inquiryProxy.getIbanInfoByCardNumber(cardNumber)
        }
        if (ibanInfoResponse.isError()) handleCardIbanInfoError(ibanInfoResponse.code)
        return ibanInfoResponse.ibanInfo ?: throw OpexError.CardIbanInfoInquiryError.exception()
    }

    private suspend fun getIbanInfoByIban(iban: String): IbanInfo {
        val ibanInfoResponse = callInquiry {
            inquiryProxy.getIbanInfoByIban(iban)
        }
        if (ibanInfoResponse.isError()) handleIbanInfoError(ibanInfoResponse.code)
        return ibanInfoResponse.ibanInfo ?: throw OpexError.IbanInfoInquiryError.exception()
    }

    private suspend fun validateBankAccountOwnership(
        uuid: String,
        cardNumber: String?,
        iban: String?
    ) {
        val bankAccounts = bankAccountPersister.findAll(cardNumber, iban)
        if (bankAccounts.isEmpty()) return

        if (bankAccounts.any { it.uuid == uuid && it.status != BankAccountStatus.REJECTED }) {
            throw OpexError.BankAccountAlreadyExist.exception()
        }

        if (bankAccounts.any { it.uuid != uuid && it.status == BankAccountStatus.VERIFIED }) {
            throw when {
                !cardNumber.isNullOrBlank() -> OpexError.CardOwnershipMismatch.exception()
                !iban.isNullOrBlank() -> OpexError.IbanOwnershipMismatch.exception()
                else -> OpexError.InvalidRequestBody.exception()
            }
        }
    }
}
