package co.nilin.opex.profile.app.service


import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.event.ProfileUpdatedEvent
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import co.nilin.opex.profile.core.data.inquiry.ComparativeResponse
import co.nilin.opex.profile.core.data.inquiry.ShahkarResponse
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.otp.*
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.*
import co.nilin.opex.profile.core.utils.handleComparativeError
import co.nilin.opex.profile.core.utils.handleShahkarError
import kotlinx.coroutines.CancellationException
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

    @Value("\${app.inquiry.mobile-identity}")
    private var mobileIdentityEnabled: Boolean,

    @Value("\${app.inquiry.personal-identity}")
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
                    kycLevel = KycLevel.LEVEL_1,
                    personType = personType
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
        profilePersister.updateUserLevelAndStatus(userId, userLevel, true)
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

        val shahkarRequested = isIranian && mobileIdentityEnabled
        val comparativeRequested = isIranian && personalIdentityEnabled

        // A provider that could not answer — timeout, refused connection, 5xx, 429 — is a problem
        // on their side, not a verdict on the user, so it comes back null instead of throwing and
        // falls back to an admin below. Anything the provider does answer is judged as before.
        val shahkarResponse = if (shahkarRequested)
            callInquiry { inquiryProxy.getShahkarInquiryResult(request.identifier, profile.mobile!!) }
        else null

        val comparativeResponse = if (comparativeRequested)
            callInquiry {
                inquiryProxy.getComparativeInquiryResult(
                    request.identifier,
                    request.birthDate,
                    request.firstName,
                    request.lastName
                )
            }
        else null

        val inquiryUnavailable = (shahkarRequested && shahkarResponse == null) ||
                (comparativeRequested && comparativeResponse == null)

        // With no admin to fall back on there is nothing that could verify this profile, and
        // auto-approving an unverified profile is not an option. Fail before anything is written.
        if (inquiryUnavailable && !isAdminApprovalRequired)
            throw OpexError.InquiryServiceUnavailable.exception()

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
                lastName = request.lastName,
                identifier = request.identifier
            )
        )

        // Anything the provider answered against the user stops here, including an error code we
        // have not mapped yet. An admin is only a fallback for a provider that never answered.
        validateInquiryResponses(shahkarResponse, comparativeResponse)

        // A response that survived validation passed, so only an unavailable inquiry is left
        val shahkarOk = !shahkarRequested || shahkarResponse != null
        val comparativeOk = !comparativeRequested || comparativeResponse != null

        val allRequestedInquiriesOk = shahkarOk && comparativeOk
        val anyInquiryRequested = shahkarRequested || comparativeRequested

        return when {
            !isAdminApprovalRequired -> approveProfileAutomatically(userId, completedProfile)
            !anyInquiryRequested -> requestAdminApproval(userId)
            allRequestedInquiriesOk -> approveProfileAutomatically(userId, completedProfile)
            else -> requestAdminApproval(userId) // An active inquiry could not be reached.
        }
    }

    /**
     * Runs an inquiry and returns null when the provider never answered. Only the call itself is
     * wrapped: the proxy raises [OpexError.InquiryServiceUnavailable] for a server-side failure and
     * transport errors for a timeout, while an answer about the user comes back as a response and
     * is judged by [validateInquiryResponses].
     */
    private suspend fun <T> callInquiry(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logger.warn("Inquiry provider did not answer: ${e.javaClass.simpleName} ${e.message}")
            null
        }
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

    suspend fun resolveUsers(request: List<String>): Map<String, String?> {
        return profilePersister.resolveUsers(request)
    }
}