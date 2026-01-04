package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.CompleteProfileRequest
import co.nilin.opex.api.core.ContactUpdateConfirmRequest
import co.nilin.opex.api.core.ContactUpdateRequest
import co.nilin.opex.api.core.ProfileApprovalUserResponse
import co.nilin.opex.api.core.inout.*

interface ProfileProxy {
    suspend fun getProfile(token: String): Profile
    suspend fun completeProfile(token: String, request: CompleteProfileRequest): Profile?
    suspend fun requestContactUpdate(token: String, request: ContactUpdateRequest): TempOtpResponse
    suspend fun confirmContactUpdate(token: String, request: ContactUpdateConfirmRequest)
    suspend fun getUserProfileApprovalRequest(token: String): ProfileApprovalUserResponse

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


    // Address Book
    suspend fun addAddressBook(token: String, request: AddAddressBookItemRequest): AddressBookResponse
    suspend fun getAllAddressBooks(token: String): List<AddressBookResponse>
    suspend fun deleteAddressBook(token: String, id: Long)
    suspend fun updateAddressBook(token: String, id: Long, request: AddAddressBookItemRequest): AddressBookResponse

    //Bank Account
    suspend fun addBankAccount(token: String, request: AddBankAccountRequest): BankAccountResponse
    suspend fun getBankAccounts(token: String): List<BankAccountResponse>
    suspend fun deleteBankAccount(token: String, id: Long)
}