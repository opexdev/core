package co.nilin.opex.profile.app.service


import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import co.nilin.opex.profile.core.data.inquiry.ComparativeResponse
import co.nilin.opex.profile.core.data.inquiry.ShahkarResponse
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.otp.*
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.*
import co.nilin.opex.profile.core.utils.handleComparativeError
import co.nilin.opex.profile.core.utils.handleShahkarError
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ProfileManagement(
    private val profilePersister: ProfilePersister, private val limitationPersister: LimitationPersister,
    private val profileApprovalRequestPersister: ProfileApprovalRequestPersister,
    private val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher,
    private val profileUpdatedPublisher: ProfileUpdatedPublisher,
    private val otpProxy: OtpProxy,
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

    suspend fun getAllProfiles(profileRequest: ProfileRequest): List<Profile> {
        return profilePersister.getAllProfile(profileRequest)
    }

    suspend fun getProfile(userId: String): Profile {
        return profilePersister.getProfile(userId)
    }

    suspend fun getHistory(userId: String, offset: Int, limit: Int): List<ProfileHistory>? {
        return profilePersister.getHistory(userId, offset, limit)
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
        ).apply { otpReceiver = OTPReceiver(mobile, OTPType.SMS) }
    }

    suspend fun updateMobile(userId: String, mobile: String, otpCode: String) {
        val verifyResponse = otpProxy.verifyOtp(
            VerifyOTPRequest(
                mobile,
                listOf(OTPCode(OTPType.SMS, otpCode))
            )
        )
        if (verifyResponse.result) {
            profileUpdatedPublisher.publish(ProfileUpdatedEvent(userId = userId, mobile = mobile))
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
        ).apply { otpReceiver = OTPReceiver(email, OTPType.EMAIL) }
    }

    suspend fun updateEmail(userId: String, email: String, otpCode: String) {
        val verifyResponse = otpProxy.verifyOtp(
            VerifyOTPRequest(
                email,
                listOf(OTPCode(OTPType.EMAIL, otpCode))
            )
        )
        if (verifyResponse.result) {
            profileUpdatedPublisher.publish(ProfileUpdatedEvent(userId = userId, email = email))
            profilePersister.updateEmail(userId, email)
        } else throw OpexError.InvalidOTP.exception()
    }

    suspend fun completeProfile(userId: String, request: CompleteProfileRequest): Profile {
        val profile = profilePersister.getProfile(userId)

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

        profileUpdatedPublisher.publish(
            ProfileUpdatedEvent(
                userId = userId,
                firstName = request.firstName,
                lastName = request.lastName
            )
        )

        validateInquiryResponses(shahkarResponse, comparativeResponse)

        if (isIranian && !isAdminApprovalRequired)
            return approveProfileAutomatically(userId, completedProfile)

        return requestAdminApproval(userId)

    }

    private suspend fun approveProfileAutomatically(userId: String, completedProfile: Profile): Profile {
        kycLevelUpdatedPublisher.publish(
            KycLevelUpdatedEvent(userId, KycLevel.LEVEL_2, LocalDateTime.now())
        )
        return profilePersister.updateStatus(userId, ProfileStatus.SYSTEM_APPROVED)
    }

    private suspend fun requestAdminApproval(userId: String): Profile {
        saveProfileApprovalRequest(userId)
        return profilePersister.updateStatus(userId, ProfileStatus.PENDING_ADMIN_APPROVAL)
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
                userId = userId,
                status = ProfileApprovalRequestStatus.PENDING,
                createDate = LocalDateTime.now()
            )
        )
    }
}