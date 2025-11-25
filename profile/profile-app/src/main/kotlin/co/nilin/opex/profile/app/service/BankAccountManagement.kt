package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.AddBankAccountRequest
import co.nilin.opex.profile.app.dto.BankAccountResponse
import co.nilin.opex.profile.app.utils.toBankAccountResponse
import co.nilin.opex.profile.core.data.inquiry.IbanInfo
import co.nilin.opex.profile.core.data.profile.BankAccount
import co.nilin.opex.profile.core.data.profile.BankAccountStatus
import co.nilin.opex.profile.core.data.profile.NationalityType
import co.nilin.opex.profile.core.spi.BankAccountPersister
import co.nilin.opex.profile.core.spi.InquiryProxy
import co.nilin.opex.profile.core.spi.ProfilePersister
import co.nilin.opex.profile.core.utils.handleCardIbanInfoError
import co.nilin.opex.profile.core.utils.handleCardOwnershipError
import co.nilin.opex.profile.core.utils.handleIbanInfoError
import co.nilin.opex.profile.core.utils.handleIbanOwnershipError
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BankAccountManagement(
    private val bankAccountPersister: BankAccountPersister,
    private val inquiryProxy: InquiryProxy,
    private val profilePersister: ProfilePersister,
    @Value("\${app.admin-approval.bank-account}")
    private var isAdminApprovalRequired: Boolean,
) {

    suspend fun addBankAccount(uuid: String, request: AddBankAccountRequest): BankAccountResponse {
        validateBankAccountOwnership(uuid, request.cardNumber, request.iban)
        val profile = profilePersister.getProfile(uuid)

        var cardNumber: String? = request.cardNumber
        var iban: String? = request.iban
        var accountNumber: String? = null
        var bank: String? = null
        var status: BankAccountStatus? = null

        if (!isAdminApprovalRequired && profile.nationality!! == (NationalityType.IRANIAN)) {
            if (!request.cardNumber.isNullOrBlank()) {
                verifyCardOwnership(request.cardNumber, profile.identifier!!, profile.birthDate!!)
                val ibanInfo = getIbanInfoByCardNumber(request.cardNumber)

                iban = ibanInfo.iban
                accountNumber = ibanInfo.depositNumber
                bank = ibanInfo.bank


            } else if (!request.iban.isNullOrBlank()) {

                verifyIbanOwnership(request.iban, profile.identifier!!, profile.birthDate!!)
                val ibanInfo = getIbanInfoByIban(request.iban)
                accountNumber = ibanInfo.depositNumber
                bank = ibanInfo.bank

            } else throw OpexError.InvalidRequestBody.exception("Either Card Number or IBAN must be provided")
            status = BankAccountStatus.VERIFIED
        }
        val bankAccount = BankAccount(
            uuid = uuid,
            name = request.name,
            cardNumber = cardNumber,
            iban = iban,
            accountNumber = accountNumber,
            bank = bank,
            status = status ?: BankAccountStatus.WAITING,
            createDate = LocalDateTime.now(),
            creator = "system"
        )

        return bankAccountPersister.save(bankAccount).toBankAccountResponse()
    }

    suspend fun getBankAccounts(uuid: String): List<BankAccountResponse> {
        return bankAccountPersister.findAll(uuid)
            .map { it.toBankAccountResponse() }
    }

    suspend fun deleteBankAccount(id: Long, uuid: String) {
        bankAccountPersister.delete(id, uuid)
    }

    suspend fun isBankAccountOwnedByUser(uuid: String, cardNumber: String?, iban: String?): Boolean {
        return !bankAccountPersister.findAll(uuid, cardNumber, iban).isEmpty()
    }

    private suspend fun verifyCardOwnership(
        cardNumber: String,
        nationalCode: String,
        birthDate: LocalDateTime
    ) {
        val ownershipResponse =
            inquiryProxy.verifyCardOwnership(cardNumber, nationalCode, birthDate)
        if (ownershipResponse.isError()) handleCardOwnershipError(ownershipResponse.code)
        if (ownershipResponse.matched == false) throw OpexError.CardOwnershipMismatch.exception()
    }

    private suspend fun verifyIbanOwnership(
        iban: String,
        nationalCode: String,
        birthDate: LocalDateTime
    ) {
        val ownershipResponse =
            inquiryProxy.verifyIbanOwnership(iban, nationalCode, birthDate)
        if (ownershipResponse.isError()) handleIbanOwnershipError(ownershipResponse.code)
        if (ownershipResponse.matched == false) throw OpexError.IbanOwnershipMismatch.exception()
    }

    private suspend fun getIbanInfoByCardNumber(cardNumber: String): IbanInfo {
        val ibanInfoResponse = inquiryProxy.getIbanInfoByCardNumber(cardNumber)
        if (ibanInfoResponse.isError()) handleCardIbanInfoError(ibanInfoResponse.code)
        return ibanInfoResponse.ibanInfo ?: throw OpexError.CardIbanInfoInquiryError.exception()
    }

    private suspend fun getIbanInfoByIban(iban: String): IbanInfo {
        val ibanInfoResponse = inquiryProxy.getIbanInfoByIban(iban)
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
