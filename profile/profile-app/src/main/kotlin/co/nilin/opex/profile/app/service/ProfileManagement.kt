package co.nilin.opex.profile.app.service


import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.otp.*
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class ProfileManagement(
    private val profilePersister: ProfilePersister,
    private val linkedAccountPersister: LinkedAccountPersister,
    private val limitationPersister: LimitationPersister,
    private val profileApprovalRequestPersister: ProfileApprovalRequestPersister,
    private val shahkarInquiry: ShahkarInquiry,
    private val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher,
    private val otpProxy: OtpProxy,
    private val authProxy: AuthProxy,
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
                    status = UserStatus.Active,
                    createDate = LocalDateTime.now(),
                    lastUpdateDate = LocalDateTime.now(),
                    creator = "system",
                    kycLevel = KycLevel.Level1
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
        profilePersister.updateUserLevel(userId, userLevel)
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

    suspend fun completeProfile(
        userId: String,
        completeProfileRequest: CompleteProfileRequest
    ): CompleteProfileResponse {
        val profile = profilePersister.getProfile(userId)?.awaitFirstOrNull()
            ?: throw OpexError.NotFound.exception("profile not found")
        if (profile.kycLevel == KycLevel.Level2) {
            throw OpexError.BadRequest.exception("Profile already completed")
        }
        val isIranian = completeProfileRequest.nationality == "Iranian"
        if (isIranian) {
            if (!shahkarInquiry.getInquiryResult(
                    completeProfileRequest.identifier,
                    profile.mobile ?: throw OpexError.BadRequest.exception("Profile mobile is empty")
                )
            ) {
                throw OpexError.VerificationFailed.exception("Mobile and identifier do not match")
            }
            completeProfileRequest.verificationStatus = true
        }
        val completedProfile = profilePersister.completeProfile(userId, completeProfileRequest).awaitFirstOrNull()
            ?: throw OpexError.BadRequest.exception("profile not found for userId: $userId")
        if (isIranian)
            kycLevelUpdatedPublisher.publish(KycLevelUpdatedEvent(userId, KycLevel.Level2, LocalDateTime.now()))
        else
            saveProfileApprovalRequest(completedProfile.id)
        return completedProfile
    }

    private suspend fun saveProfileApprovalRequest(profileId: Long) {
        profileApprovalRequestPersister.save(
            ProfileApprovalRequest(
                profileId = profileId,
                status = ProfileApprovalRequestStatus.PENDING,
                createDate = LocalDateTime.now(),
                updateDate = null,
                updater = null
            )
        )
    }
}