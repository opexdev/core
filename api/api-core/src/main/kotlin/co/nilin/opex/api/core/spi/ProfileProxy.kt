package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.CompleteProfileRequest
import co.nilin.opex.api.core.inout.ContactUpdateConfirmRequest
import co.nilin.opex.api.core.inout.ContactUpdateRequest
import co.nilin.opex.api.core.inout.ProfileApprovalRequestUser
import co.nilin.opex.api.core.inout.*

import java.util.UUID

interface ProfileProxy {
    suspend fun getProfile(token: String): Profile
    suspend fun completeProfile(token: String, request: CompleteProfileRequest): Profile?
    suspend fun requestContactUpdate(token: String, request: ContactUpdateRequest): TempOtpResponse
    suspend fun confirmContactUpdate(token: String, request: ContactUpdateConfirmRequest)
    suspend fun getUserProfileApprovalRequest(token: String): ProfileApprovalRequestUser

    // Admin
    suspend fun getProfiles(token: String, profileRequest: ProfileRequest): List<Profile>
    suspend fun getProfileAdmin(token: String, uuid: String): Profile
    suspend fun getProfileHistory(token: String, uuid: String, limit: Int, offset: Int): List<ProfileHistory>
    suspend fun getProfileApprovalRequests(
        token: String,
        request: ProfileApprovalRequestFilter
    ): List<ProfileApprovalAdminResponse>

    suspend fun getProfileApprovalRequest(token: String, requestId: Long): ProfileApprovalAdminResponse
    suspend fun updateProfileApprovalRequest(
        token: String,
        request: UpdateApprovalRequestBody
    ): ProfileApprovalAdminResponse

    suspend fun resolveUsers(request: ResolveUsersRequest): Map<String, String?>

    // Address Book
    suspend fun addAddressBook(token: String, request: AddAddressBookItemRequest): AddressBookResponse
    suspend fun getAllAddressBooks(token: String): List<AddressBookResponse>
    suspend fun deleteAddressBook(token: String, id: Long)
    suspend fun updateAddressBook(token: String, id: Long, request: AddAddressBookItemRequest): AddressBookResponse

    //Company
    suspend fun getCompany(token: String): CompanyResponse
    suspend fun ensureLegalAccountAvailable(token: String, request: LegalAccountAvailabilityRequest)
    suspend fun registerLegalAccount(token: String, request: RegisterLegalAccountRequest): CompanyAdminResponse
    suspend fun getCompanyAdmin(token: String, uuid: String): CompanyAdminResponse
    suspend fun getCompanyByUuid(token: String, companyUuid: UUID): CompanyAdminResponse
    suspend fun getCompanyByNationalId(token: String, nationalId: String): CompanyAdminResponse
    suspend fun updateCompany(token: String, companyUuid: UUID, request: UpdateCompanyRequest): CompanyAdminResponse

    //Bank Account
    suspend fun addBankAccount(token: String, request: AddBankAccountRequest): BankAccountResponse
    suspend fun getBankAccounts(token: String): List<BankAccountResponse>
    suspend fun deleteBankAccount(token: String, id: Long)

    suspend fun getUserBankAccountApprovalRequest(token: String, bankAccountId: Long): BankAccountApprovalUserResponse

    //Bank Account admin management
    suspend fun getBankAccountsOfUser(token: String, uuid: String): List<BankAccountAdminResponse>

    suspend fun addBankAccountForUser(
        token: String,
        uuid: String,
        request: AddBankAccountAdminRequest
    ): BankAccountAdminResponse

    suspend fun deleteBankAccountOfUser(token: String, uuid: String, id: Long)

    //Bank Account approval requests
    suspend fun getBankAccountApprovalRequests(
        token: String,
        request: BankAccountApprovalRequestFilter
    ): List<BankAccountApprovalAdminResponse>

    suspend fun getBankAccountApprovalRequest(token: String, requestId: Long): BankAccountApprovalAdminResponse

    suspend fun updateBankAccountApprovalRequest(
        token: String,
        request: UpdateBankAccountApprovalBody
    ): BankAccountApprovalAdminResponse

    //Manual identity inquiries
    suspend fun shahkarInquiry(token: String, request: ShahkarInquiryRequest): ShahkarInquiryResponse

    suspend fun identitySimilarityInquiry(
        token: String,
        request: IdentitySimilarityInquiryRequest
    ): IdentitySimilarityInquiryResponse

    suspend fun cardOwnershipInquiry(
        token: String,
        request: CardOwnershipInquiryRequest
    ): VerifyOwnershipInquiryResponse

    suspend fun ibanOwnershipInquiry(
        token: String,
        request: IbanOwnershipInquiryRequest
    ): VerifyOwnershipInquiryResponse

    suspend fun cardInfoInquiry(token: String, request: CardInfoInquiryRequest): IbanInfoInquiryResponse

    suspend fun ibanInfoInquiry(token: String, request: IbanInfoInquiryRequest): IbanInfoInquiryResponse
}