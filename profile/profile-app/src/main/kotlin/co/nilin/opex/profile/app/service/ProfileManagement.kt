package co.nilin.opex.profile.app.service


import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.otp.*
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.*
import co.nilin.opex.profile.core.utils.handleComparativeError
import co.nilin.opex.profile.core.utils.handleShahkarError
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class ProfileManagement(
    private val profilePersister: ProfilePersister,
    private val linkedAccountPersister: LinkedAccountPersister,
    private val limitationPersister: LimitationPersister,
    private val profileApprovalRequestPersister: ProfileApprovalRequestPersister,
    private val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher,
    private val otpProxy: OtpProxy,
    private val authProxy: AuthProxy,
    private val inquiryProxy: InquiryProxy,

    @Value("\${app.inquiry.mobile-indentiy}")
    private var mobileIdentityEnabled: Boolean,

    @Value("\${app.inquiry.personal-indentiy}")
    private var personalIdentityEnabled: Boolean,

    @Value("\${app.admin-approval.profile-completion-request}")
    private var isAdminApprovalRequired: Boolean,
) {
    private val logger = LoggerFactory.getLogger(ProfileManagement::class.java)
    suspend fun registerNewUser(event: UserCreatedEvent) {
        with(event) {
            profilePersister.createProfile(
                Profile(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    mobile = mobile,
                    userId = uuid,
                    status = ProfileStatus.CREATED,
                    createDate = LocalDateTime.now(),
                    lastUpdateDate = LocalDateTime.now(),
                    creator = "system",
                    kycLevel = KycLevel.LEVEL_1
                )
            )
        }
    }

    suspend fun getAllProfiles(offset: Int, size: Int, profileRequest: ProfileRequest): List<Profile?>? {
        profileRequest.accountNumber?.let {
            val res = profilePersister.getAllProfile(offset, size, profileRequest)?.toList()
            val accountOwner =
                linkedAccountPersister.getOwner(profileRequest.accountNumber!!, profileRequest.partialSearch)
                    ?.map { profilePersister.getProfile(it.userId)?.awaitFirstOrNull() }?.toList()
            if (res?.isEmpty() == true || accountOwner?.isEmpty() == true) {
                return null
            } else {
                return addDetail(accountOwner!!::contains?.let { it1 -> res?.filter(it1) }, profileRequest)
            }
        } ?: run {
            return addDetail(profilePersister.getAllProfile(offset, size, profileRequest)?.toList(), profileRequest)

        }
    }


    private suspend fun addDetail(res: List<Profile?>?, profileRequest: ProfileRequest): List<Profile?>? {
        if (profileRequest.includeLinkedAccount == true) {
            res?.forEach {
                it?.linkedAccounts = linkedAccountPersister.getAccounts(it?.userId!!)?.toList()
            }
        }
        if (profileRequest.includeLimitation == true) {
            res?.forEach {
                it?.limitations = limitationPersister.getLimitation(it?.userId)?.toList()
            }
        }
        return res;
    }

    suspend fun getProfile(userId: String): Mono<Profile>? {
        return profilePersister.getProfile(userId)
    }

    suspend fun update(userId: String, newProfile: UpdateProfileRequest): Mono<Profile>? {
        return profilePersister.updateProfile(userId, newProfile)
    }

    suspend fun updateAsAdmin(userId: String, newProfile: Profile): Mono<Profile>? {
        return profilePersister.updateProfileAsAdmin(userId, newProfile)
    }

    suspend fun create(userId: String, newProfile: Profile): Mono<Profile>? {
        newProfile.userId = userId
        return profilePersister.createProfile(newProfile)
    }

    suspend fun getHistory(userId: String, offset: Int, size: Int): List<ProfileHistory>? {
        return profilePersister.getHistory(userId, offset, size)
    }

    suspend fun updateUserLevel(userId: String, userLevel: KycLevel) {
        profilePersister.updateUserLevelAndStatus(userId, userLevel)
    }

    suspend fun requestUpdateMobile(userId: String, mobile: String): TempOtpResponse {
        profilePersister.validateMobileForUpdate(userId, mobile)
        return otpProxy.requestOtp(
            NewOTPRequest(
                mobile,
                listOf(OTPReceiver(mobile, OTPType.SMS)),
                OTPAction.UPDATE_MOBILE.name
            )
        )
    }

    suspend fun updateMobile(userId: String, mobile: String, otpCode: String) {
        val verifyResponse = otpProxy.verifyOtp(
            VerifyOTPRequest(
                mobile,
                listOf(OTPCode(OTPType.SMS, otpCode))
            )
        )
        if (verifyResponse.result) {
            authProxy.updateMobile(userId, mobile)
            profilePersister.updateMobile(userId, mobile)
        } else throw OpexError.InvalidOTP.exception()
    }

    suspend fun requestUpdateEmail(userId: String, email: String): TempOtpResponse {
        profilePersister.validateEmailForUpdate(userId, email)
        return otpProxy.requestOtp(
            NewOTPRequest(
                email,
                listOf(OTPReceiver(email, OTPType.EMAIL)),
                OTPAction.UPDATE_EMAIL.name
            )
        )
    }

    suspend fun updateEmail(userId: String, email: String, otpCode: String) {
        val verifyResponse = otpProxy.verifyOtp(
            VerifyOTPRequest(
                email,
                listOf(OTPCode(OTPType.EMAIL, otpCode))
            )
        )
        if (verifyResponse.result) {
            authProxy.updateEmail(userId, email)
            profilePersister.updateEmail(userId, email)
        } else throw OpexError.InvalidOTP.exception()
    }

    suspend fun completeProfile(userId: String, request: CompleteProfileRequest): Profile {
        val profile = profilePersister.getProfile(userId)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()

        if (profile.kycLevel == KycLevel.LEVEL_2) {
            throw OpexError.ProfileAlreadyCompleted.exception()
        }

        val isIranian = request.nationality == NationalityType.IRANIAN

        val shahkarResponse = if (isIranian && mobileIdentityEnabled) {
            inquiryProxy.getShahkarInquiryResult(request.identifier, profile.mobile!!)
        } else null

        val comparativeResponse = if (isIranian && personalIdentityEnabled) {
            inquiryProxy.getComparativeInquiryResult(
                request.identifier,
                request.birthDate,
                request.firstName,
                request.lastName
            )
        } else null

        val isMobileIdentityMatch = shahkarResponse?.let {
            !it.isError() && it.matched == true
        }

        val isPersonalIdentityMatch = comparativeResponse?.let {
            !it.isError() &&
                    (it.firstNameSimilarityPercentage ?: 0) >= 95 &&
                    (it.lastNameSimilarityPercentage ?: 0) >= 95
        }

        val completedProfile = updateProfile(userId, request, isMobileIdentityMatch, isPersonalIdentityMatch)

        authProxy.updateName(userId, request.firstName, request.lastName)

        validateInquiryResponses(shahkarResponse, comparativeResponse)

        if (isIranian && !isAdminApprovalRequired) {
            approveProfileAutomatically(userId, completedProfile)
        } else {
            requestAdminApproval(userId)
        }
        return completedProfile
    }

    private suspend fun approveProfileAutomatically(userId: String, completedProfile: Profile) {
        kycLevelUpdatedPublisher.publish(
            KycLevelUpdatedEvent(userId, KycLevel.LEVEL_2, LocalDateTime.now())
        )
        completedProfile.kycLevel = KycLevel.LEVEL_2
        completedProfile.status = ProfileStatus.SYSTEM_APPROVED
        profilePersister.updateStatus(userId, ProfileStatus.SYSTEM_APPROVED)
    }

    private suspend fun requestAdminApproval(userId: String) {
        saveProfileApprovalRequest(userId)
        profilePersister.updateStatus(userId, ProfileStatus.PENDING_ADMIN_APPROVAL)
    }

    suspend fun updateProfile(
        userId: String,
        request: CompleteProfileRequest,
        isMobileIdentityMatch: Boolean?,
        isPersonalIdentityMatch: Boolean?
    ): Profile = coroutineScope {
        profilePersister.completeProfile(userId, request, isMobileIdentityMatch, isPersonalIdentityMatch).awaitFirst()
    }

    private fun validateInquiryResponses(
        shahkarResponse: ShahkarResponse?,
        comparativeResponse: ComparativeResponse?
    ) {
        shahkarResponse?.let {
            if (it.isError()) handleShahkarError(it.code)
            if (it.matched == false) throw OpexError.ShahkarVerificationFailed.exception()
        }

        comparativeResponse?.let {
            if (it.isError()) handleComparativeError(it.code)
            if ((it.firstNameSimilarityPercentage ?: 0) < 95)
                throw OpexError.FirstNameIsNotSimilarEnough.exception()
            if ((it.lastNameSimilarityPercentage ?: 0) < 95)
                throw OpexError.LastNameIsNotSimilarEnough.exception()
        }
    }

    private suspend fun saveProfileApprovalRequest(userId: String) {
        profileApprovalRequestPersister.save(
            ProfileApprovalRequest(
                profileId = profilePersister.getProfileId(userId),
                status = ProfileApprovalRequestStatus.PENDING,
                createDate = LocalDateTime.now()
            )
        )
    }
}