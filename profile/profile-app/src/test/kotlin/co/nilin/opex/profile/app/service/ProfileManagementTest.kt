package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.sample.VALID
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.otp.*
import co.nilin.opex.profile.core.data.profile.ComparativeResponse
import co.nilin.opex.profile.core.data.profile.ProfileStatus
import co.nilin.opex.profile.core.data.profile.ShahkarResponse
import co.nilin.opex.profile.core.spi.*
import co.nilin.opex.utility.error.data.OpexException
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class ProfileManagementTest {

    private val profilePersister = mockk<ProfilePersister>()
    private val linkedAccountPersister = mockk<LinkedAccountPersister>()
    private val limitationPersister = mockk<LimitationPersister>()
    private val profileApprovalRequestPersister = mockk<ProfileApprovalRequestPersister>()
    private val kycLevelUpdatedPublisher = mockk<KycLevelUpdatedPublisher>()
    private val otpProxy = mockk<OtpProxy>()
    private val authProxy = mockk<AuthProxy>()
    private val inquiryProxy = mockk<InquiryProxy>()

    private lateinit var profileManagement: ProfileManagement

    companion object {
        private const val USER_ID = "user-123"
        private const val MOBILE = "09120000001"
        private const val EMAIL = "user@example.com"
        private const val VALID_OTP = "123456"
        private const val INVALID_OTP = "000000"

        private fun smsOtpRequest(mobile: String, code: String) =
            VerifyOTPRequest(mobile, listOf(OTPCode(OTPType.SMS, code)))

        private fun emailOtpRequest(email: String, code: String) =
            VerifyOTPRequest(email, listOf(OTPCode(OTPType.EMAIL, code)))
    }

    @BeforeEach
    fun setup() {
        clearAllMocks()
        profileManagement = spyk(
            ProfileManagement(
                profilePersister,
                linkedAccountPersister,
                limitationPersister,
                profileApprovalRequestPersister,
                kycLevelUpdatedPublisher,
                otpProxy,
                authProxy,
                inquiryProxy,
                true,
                true,
                false
            )
        )
    }

    @Nested
    inner class UserRegistrationTests {
        @Test
        fun `should create profile when user created event is valid`() = runBlocking {
            val event = UserCreatedEvent(USER_ID, null, null, null, MOBILE)
            coEvery { profilePersister.createProfile(any()) } returns Mono.just(VALID.profile)

            profileManagement.registerNewUser(event)

            coVerify {
                profilePersister.createProfile(
                    match {
                        it.userId == USER_ID && it.mobile == MOBILE && it.status == ProfileStatus.CREATED && it.kycLevel == KycLevel.LEVEL_1 && it.creator == "system"
                    })
            }
        }
    }

    @Nested
    inner class MobileUpdateTests {

        @Test
        fun `should request OTP when mobile update requested`() = runBlocking {
            val expectedResponse = TempOtpResponse(VALID_OTP)

            coEvery { profilePersister.validateMobileForUpdate(USER_ID, MOBILE) } returns Unit
            coEvery { otpProxy.requestOtp(any()) } returns expectedResponse

            val result = profileManagement.requestUpdateMobile(USER_ID, MOBILE)

            Assertions.assertEquals(expectedResponse, result)
            coVerify { profilePersister.validateMobileForUpdate(USER_ID, MOBILE) }
            coVerify { otpProxy.requestOtp(any()) }
        }

        @Test
        fun `should update auth and profile when OTP is valid`() = runBlocking {
            coEvery { otpProxy.verifyOtp(smsOtpRequest(MOBILE, VALID_OTP)) } returns OTPVerifyResponse(
                true, OTPResultType.VALID
            )
            coEvery { authProxy.updateMobile(USER_ID, MOBILE) } returns Unit
            coEvery { profilePersister.updateMobile(USER_ID, MOBILE) } returns Unit

            profileManagement.updateMobile(USER_ID, MOBILE, VALID_OTP)

            coVerify { otpProxy.verifyOtp(smsOtpRequest(MOBILE, VALID_OTP)) }
            coVerify { authProxy.updateMobile(USER_ID, MOBILE) }
            coVerify { profilePersister.updateMobile(USER_ID, MOBILE) }
        }

        @Test
        fun `should throw InvalidOTP when OTP is invalid`() = runBlocking {
            coEvery { otpProxy.verifyOtp(smsOtpRequest(MOBILE, INVALID_OTP)) } returns OTPVerifyResponse(
                false, OTPResultType.INVALID
            )

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.updateMobile(USER_ID, MOBILE, INVALID_OTP) }
            }

            Assertions.assertEquals(OpexError.InvalidOTP, ex.error)
            coVerify { otpProxy.verifyOtp(smsOtpRequest(MOBILE, INVALID_OTP)) }
            coVerify(exactly = 0) { authProxy.updateMobile(any(), any()) }
            coVerify(exactly = 0) { profilePersister.updateMobile(any(), any()) }
        }
    }

    @Nested
    inner class EmailUpdateTests {

        @Test
        fun `should request OTP when email update requested`() = runBlocking {
            val expectedResponse = TempOtpResponse(VALID_OTP)

            coEvery { profilePersister.validateEmailForUpdate(USER_ID, EMAIL) } returns Unit
            coEvery { otpProxy.requestOtp(any()) } returns expectedResponse

            val result = profileManagement.requestUpdateEmail(USER_ID, EMAIL)

            Assertions.assertEquals(expectedResponse, result)
            coVerify { profilePersister.validateEmailForUpdate(USER_ID, EMAIL) }
            coVerify { otpProxy.requestOtp(any()) }
        }

        @Test
        fun `should update auth and profile when OTP is valid`() = runBlocking {
            coEvery { otpProxy.verifyOtp(emailOtpRequest(EMAIL, VALID_OTP)) } returns OTPVerifyResponse(
                true, OTPResultType.VALID
            )
            coEvery { authProxy.updateEmail(USER_ID, EMAIL) } returns Unit
            coEvery { profilePersister.updateEmail(USER_ID, EMAIL) } returns Unit

            profileManagement.updateEmail(USER_ID, EMAIL, VALID_OTP)

            coVerify { otpProxy.verifyOtp(emailOtpRequest(EMAIL, VALID_OTP)) }
            coVerify { authProxy.updateEmail(USER_ID, EMAIL) }
            coVerify { profilePersister.updateEmail(USER_ID, EMAIL) }
        }

        @Test
        fun `should throw InvalidOTP when OTP is invalid`() = runBlocking {
            coEvery { otpProxy.verifyOtp(emailOtpRequest(EMAIL, INVALID_OTP)) } returns OTPVerifyResponse(
                false, OTPResultType.INVALID
            )

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.updateEmail(USER_ID, EMAIL, INVALID_OTP) }
            }

            Assertions.assertEquals(OpexError.InvalidOTP, ex.error)
            coVerify { otpProxy.verifyOtp(emailOtpRequest(EMAIL, INVALID_OTP)) }
            coVerify(exactly = 0) { authProxy.updateEmail(any(), any()) }
            coVerify(exactly = 0) { profilePersister.updateEmail(any(), any()) }
        }
    }

    @Nested
    inner class CompleteProfileTests {

        @Test
        fun `should throw ProfileNotFound if profile not exists`() = runBlocking {
            coEvery { profilePersister.getProfile(USER_ID) } returns Mono.empty()

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.completeProfile(USER_ID, VALID.iranianRequest) }
            }

            Assertions.assertEquals(OpexError.ProfileNotfound, ex.error)
        }

        @Test
        fun `should throw ProfileAlreadyCompleted if profile already has higher KYC`() = runBlocking {
            val completedProfile = VALID.newProfile.copy(kycLevel = KycLevel.LEVEL_2)
            coEvery { profilePersister.getProfile(USER_ID) } returns Mono.just(completedProfile)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.completeProfile(USER_ID, VALID.iranianRequest) }
            }

            Assertions.assertEquals(OpexError.ProfileAlreadyCompleted, ex.error)
        }

        @Test
        fun `should auto approve non-Iranian if auto-approval enabled`() = runBlocking {
            coEvery { profilePersister.getProfile(USER_ID) } returns Mono.just(VALID.newProfile)
            coEvery { profilePersister.getProfileId(USER_ID) } returns 1L
            coEvery { profileManagement.updateProfile(any(), any(), any(), any()) } returns VALID.newProfile
            coEvery { authProxy.updateName(any(), any(), any()) } returns Unit
            coEvery { profileApprovalRequestPersister.save(any()) } returns Mono.just(VALID.profileApprovalRequest)
            coEvery { profilePersister.updateStatus(any(), any()) } returns Unit

            val result = profileManagement.completeProfile(USER_ID, VALID.foreignRequest)

            Assertions.assertEquals(VALID.newProfile, result)
            coVerify { profileApprovalRequestPersister.save(any()) }
            coVerify(exactly = 0) { inquiryProxy.getShahkarInquiryResult(any(), any()) }
            coVerify(exactly = 0) { inquiryProxy.getComparativeInquiryResult(any(), any(), any(), any()) }
        }

        @Test
        fun `should invoke Shahkar and Comparative for Iranian if checks enabled`() = runBlocking {
            coEvery { profilePersister.getProfile(USER_ID) } returns Mono.just(VALID.contactInfoUpdatedProfile)
            coEvery { inquiryProxy.getShahkarInquiryResult(any(), any()) } returns ShahkarResponse(true)
            coEvery {
                inquiryProxy.getComparativeInquiryResult(
                    any(), any(), any(), any()
                )
            } returns ComparativeResponse(96, 96)
            coEvery { profileManagement.updateProfile(any(), any(), any(), any()) } returns VALID.profile
            coEvery { authProxy.updateName(any(), any(), any()) } returns Unit
            coEvery { profileApprovalRequestPersister.save(any()) } returns Mono.just(VALID.profileApprovalRequest)
            coEvery { profilePersister.updateStatus(any(), any()) } returns Unit
            coEvery { kycLevelUpdatedPublisher.publish(any()) } returns Unit

            profileManagement.completeProfile(USER_ID, VALID.iranianRequest)

            coVerify { inquiryProxy.getShahkarInquiryResult("1234567890", "09120000000") }
            coVerify {
                inquiryProxy.getComparativeInquiryResult(
                    "1234567890", VALID.iranianRequest.birthDate, "firstname", "lastname"
                )
            }
        }

        @Test
        fun `should skip Shahkar and Comparative if checks disabled`() = runBlocking {
            profileManagement = spyk(
                ProfileManagement(
                    profilePersister,
                    linkedAccountPersister,
                    limitationPersister,
                    profileApprovalRequestPersister,
                    kycLevelUpdatedPublisher,
                    otpProxy,
                    authProxy,
                    inquiryProxy,
                    false,
                    false,
                    false
                )
            )
            coEvery { profilePersister.getProfile(USER_ID) } returns Mono.just(VALID.contactInfoUpdatedProfile)
            coEvery { profileManagement.updateProfile(any(), any(), any(), any()) } returns VALID.profile
            coEvery { authProxy.updateName(any(), any(), any()) } returns Unit
            coEvery { profileApprovalRequestPersister.save(any()) } returns Mono.just(VALID.profileApprovalRequest)
            coEvery { profilePersister.updateStatus(any(), any()) } returns Unit
            coEvery { kycLevelUpdatedPublisher.publish(any()) } returns Unit

            profileManagement.completeProfile(USER_ID, VALID.iranianRequest)

            coVerify(exactly = 0) { inquiryProxy.getShahkarInquiryResult(any(), any()) }
            coVerify(exactly = 0) { inquiryProxy.getComparativeInquiryResult(any(), any(), any(), any()) }
        }

        @Test
        fun `should approve automatically if Shahkar and Comparative valid`() = runBlocking {
            coEvery { profilePersister.getProfile(USER_ID) } returns Mono.just(VALID.contactInfoUpdatedProfile)
            coEvery { inquiryProxy.getShahkarInquiryResult(any(), any()) } returns ShahkarResponse(true)
            coEvery {
                inquiryProxy.getComparativeInquiryResult(
                    any(), any(), any(), any()
                )
            } returns ComparativeResponse(98, 97)
            coEvery { profileManagement.updateProfile(any(), any(), eq(true), eq(true)) } returns VALID.profile
            coEvery { authProxy.updateName(any(), any(), any()) } returns Unit
            coEvery { profilePersister.updateStatus(any(), any()) } returns Unit
            coEvery { kycLevelUpdatedPublisher.publish(any()) } returns Unit

            val result = profileManagement.completeProfile(USER_ID, VALID.iranianRequest)

            Assertions.assertEquals(VALID.profile, result)
            coVerify { profilePersister.updateStatus(USER_ID, ProfileStatus.SYSTEM_APPROVED) }
        }

        @Test
        fun `should request admin approval if required even when Shahkar and Comparative valid`() = runBlocking {
            profileManagement = spyk(
                ProfileManagement(
                    profilePersister,
                    linkedAccountPersister,
                    limitationPersister,
                    profileApprovalRequestPersister,
                    kycLevelUpdatedPublisher,
                    otpProxy,
                    authProxy,
                    inquiryProxy,
                    true,
                    true,
                    true
                )
            )
            coEvery { profilePersister.getProfile(USER_ID) } returns Mono.just(VALID.contactInfoUpdatedProfile)
            coEvery { profilePersister.getProfileId(USER_ID) } returns 1L
            coEvery { inquiryProxy.getShahkarInquiryResult(any(), any()) } returns ShahkarResponse(true)
            coEvery {
                inquiryProxy.getComparativeInquiryResult(
                    any(), any(), any(), any()
                )
            } returns ComparativeResponse(96, 96)
            coEvery { profileManagement.updateProfile(any(), any(), any(), any()) } returns VALID.profile
            coEvery { authProxy.updateName(any(), any(), any()) } returns Unit
            coEvery { profileApprovalRequestPersister.save(any()) } returns Mono.just(VALID.profileApprovalRequest)
            coEvery { profilePersister.updateStatus(any(), any()) } returns Unit
            coEvery { kycLevelUpdatedPublisher.publish(any()) } returns Unit

            val result = profileManagement.completeProfile(USER_ID, VALID.iranianRequest)

            Assertions.assertEquals(VALID.profile, result)
            coVerify { profileApprovalRequestPersister.save(any()) }
        }
    }
}
