package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.profile.ProfileStatus
import co.nilin.opex.profile.ports.postgres.dao.ProfileHistoryRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import co.nilin.opex.profile.ports.postgres.sample.VALID
import co.nilin.opex.utility.error.data.OpexException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import reactor.core.publisher.Mono

class ProfileManagementTest {

    private val profileRepository = mockk<ProfileRepository>()
    private val profileHistoryRepository = mockk<ProfileHistoryRepository>()
    private val profileManagement =
        ProfileManagementImp(profileRepository, profileHistoryRepository, "^09\\d{9}$")

    companion object {
        private const val USER_ID = "user-123"
        private const val MOBILE = "09120000000"
        private const val NEW_MOBILE = "09120000001"
        private const val EMAIL = "user@example.com"
        private const val NEW_EMAIL = "new@example.com"
    }

    @Nested
    inner class CreateProfileTests {

        @Test
        fun `should throw BadRequest when email and mobile are null`() = runBlocking {
            val profile = VALID.profile.copy(email = null, mobile = null)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.createProfile(profile).block() }
            }

            Assertions.assertEquals(OpexError.BadRequest, ex.error)
        }

        @Test
        fun `should throw UserIdAlreadyExists when profile already exists`() = runBlocking {
            every {
                profileRepository.findByUserIdOrEmailOrMobile(USER_ID, EMAIL, MOBILE)
            } returns Mono.just(VALID.profileModel)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.createProfile(VALID.profile).block() }
            }

            Assertions.assertEquals(OpexError.UserIdAlreadyExists, ex.error)
        }

        @Test
        fun `should save and return profile when valid`() = runBlocking {
            every {
                profileRepository.findByUserIdOrEmailOrMobile(USER_ID, EMAIL, MOBILE)
            } returns Mono.empty()
            every { profileRepository.save(any()) } returns Mono.just(VALID.profileModel)

            val result = profileManagement.createProfile(VALID.profile).block()!!

            Assertions.assertEquals(USER_ID, result.userId)
            Assertions.assertEquals(EMAIL, result.email)
            Assertions.assertEquals(MOBILE, result.mobile)

            verify(exactly = 1) { profileRepository.save(any()) }
        }
    }

    @Nested
    inner class ValidateEmailTests {

        @Test
        fun `should throw ProfileNotFound when profile not exists`() = runBlocking {
            every { profileRepository.findByUserId(USER_ID) } returns Mono.empty()

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.validateEmailForUpdate(USER_ID, EMAIL) }
            }

            Assertions.assertEquals(OpexError.ProfileNotfound, ex.error)
        }

        @Test
        fun `should throw EmailAlreadySet when profile already has email`() = runBlocking {
            every { profileRepository.findByUserId(USER_ID) } returns Mono.just(VALID.profileModel)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.validateEmailForUpdate(USER_ID, EMAIL) }
            }

            Assertions.assertEquals(OpexError.EmailAlreadySet, ex.error)
        }

        @Test
        fun `should pass when email not set`() = runBlocking {
            every { profileRepository.findByUserId(USER_ID) } returns Mono.just(VALID.profileModelWithoutEmail)

            profileManagement.validateEmailForUpdate(USER_ID, NEW_EMAIL)

            verify { profileRepository.findByUserId(USER_ID) }
        }

        @Test
        fun `should throw InvalidEmail when email format invalid`() {
            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.validateEmailForUpdate(USER_ID, "invalid.com") }
            }
            Assertions.assertEquals(OpexError.InvalidEmail, ex.error)
        }
    }

    @Nested
    inner class ValidateMobileTests {

        @Test
        fun `should throw ProfileNotFound when profile not exists`() = runBlocking {
            every { profileRepository.findByUserId(USER_ID) } returns Mono.empty()

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.validateMobileForUpdate(USER_ID, MOBILE) }
            }

            Assertions.assertEquals(OpexError.ProfileNotfound, ex.error)
        }

        @Test
        fun `should throw MobileAlreadySet when profile already has mobile`() = runBlocking {
            every { profileRepository.findByUserId(USER_ID) } returns Mono.just(VALID.profileModel)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.validateMobileForUpdate(USER_ID, MOBILE) }
            }

            Assertions.assertEquals(OpexError.MobileAlreadySet, ex.error)
        }

        @Test
        fun `should throw InvalidMobile when mobile format invalid`() {
            val profileModel = ProfileModel(1).apply {
                userId = USER_ID
                mobile = "0912000"
            }

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.validateMobileForUpdate(profileModel.userId, profileModel.mobile!!) }
            }

            Assertions.assertEquals(OpexError.InvalidMobile, ex.error)
        }

        @Test
        fun `should pass when mobile not set`() = runBlocking {
            every { profileRepository.findByUserId(USER_ID) } returns Mono.just(VALID.profileModelWithoutMobile)

            profileManagement.validateMobileForUpdate(USER_ID, NEW_MOBILE)

            verify { profileRepository.findByUserId(USER_ID) }
        }
    }

    @Nested
    inner class UpdateMobileTests {

        @Test
        fun `should update profile when mobile valid`() = runBlocking {
            val profileModel = VALID.profileModelWithoutMobile.copy()

            every { profileRepository.findByMobile(NEW_MOBILE) } returns Mono.empty()
            every { profileRepository.findByUserId(USER_ID) } returns Mono.just(profileModel)
            every { profileRepository.save(any()) } returns Mono.just(profileModel)

            profileManagement.updateMobile(USER_ID, NEW_MOBILE)

            Assertions.assertEquals(NEW_MOBILE, profileModel.mobile)
            Assertions.assertEquals(ProfileStatus.CONTACT_INFO_COMPLETED, profileModel.status)
        }

        @Test
        fun `should throw MobileAlreadyExists when mobile already exists`() = runBlocking {
            every { profileRepository.findByMobile(NEW_MOBILE) } returns Mono.just(VALID.profileModel)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.updateMobile(USER_ID, NEW_MOBILE) }
            }

            Assertions.assertEquals(OpexError.MobileAlreadyExists, ex.error)
        }

        @Test
        fun `should throw ProfileNotFound when user not exists`() = runBlocking {
            every { profileRepository.findByMobile(NEW_MOBILE) } returns Mono.empty()
            every { profileRepository.findByUserId(USER_ID) } returns Mono.empty()

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.updateMobile(USER_ID, NEW_MOBILE) }
            }

            Assertions.assertEquals(OpexError.ProfileNotfound, ex.error)
        }
    }

    @Nested
    inner class UpdateEmailTests {

        @Test
        fun `should update profile when email valid`() = runBlocking {
            val profileModel = VALID.profileModelWithoutEmail.copy()

            every { profileRepository.findByEmail(NEW_EMAIL) } returns Mono.empty()
            every { profileRepository.findByUserId(USER_ID) } returns Mono.just(profileModel)
            every { profileRepository.save(any()) } returns Mono.just(profileModel)

            profileManagement.updateEmail(USER_ID, NEW_EMAIL)

            Assertions.assertEquals(NEW_EMAIL, profileModel.email)
            Assertions.assertEquals(ProfileStatus.CONTACT_INFO_COMPLETED, profileModel.status)
        }

        @Test
        fun `should throw EmailAlreadyExists when email already exists`() = runBlocking {
            every { profileRepository.findByEmail("dup@example.com") } returns Mono.just(VALID.profileModel)

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.updateEmail(USER_ID, "dup@example.com") }
            }

            Assertions.assertEquals(OpexError.EmailAlreadyExists, ex.error)
        }

        @Test
        fun `should throw ProfileNotFound when user not exists`() = runBlocking {
            every { profileRepository.findByEmail(NEW_EMAIL) } returns Mono.empty()
            every { profileRepository.findByUserId(USER_ID) } returns Mono.empty()

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.updateEmail(USER_ID, NEW_EMAIL) }
            }

            Assertions.assertEquals(OpexError.ProfileNotfound, ex.error)
        }
    }

    @Nested
    inner class UpdateStatusTests {

        @Test
        fun `should update status when user exists`() = runBlocking {
            val profileModel = VALID.profileModelWithoutEmail.copy()
            val newStatus = ProfileStatus.CONTACT_INFO_COMPLETED

            every { profileRepository.findByUserId(USER_ID) } returns Mono.just(profileModel)
            every { profileRepository.save(any()) } returns Mono.just(profileModel)

            profileManagement.updateStatus(USER_ID, newStatus)

            Assertions.assertEquals(newStatus, profileModel.status)
        }

        @Test
        fun `should throw ProfileNotFound when user not exists`() = runBlocking {
            every { profileRepository.findByUserId(USER_ID) } returns Mono.empty()

            val ex = Assertions.assertThrows(OpexException::class.java) {
                runBlocking { profileManagement.updateStatus(USER_ID, ProfileStatus.CONTACT_INFO_COMPLETED) }
            }

            Assertions.assertEquals(OpexError.ProfileNotfound, ex.error)
        }
    }
}
