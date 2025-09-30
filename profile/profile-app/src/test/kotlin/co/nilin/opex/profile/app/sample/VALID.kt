package co.nilin.opex.profile.app.sample

import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import java.time.LocalDateTime

object VALID {
    val newProfile = Profile(
        userId = "user-123",
        firstName = "firstname",
        lastName = "lastname",
        email = "user@example.com",
        status = ProfileStatus.CREATED,
        kycLevel = KycLevel.LEVEL_1
    )

    val contactInfoUpdatedProfile = Profile(
        userId = "user-123",
        firstName = "firstname",
        lastName = "lastname",
        email = "user@example.com",
        mobile = "09120000000",
        status = ProfileStatus.CONTACT_INFO_COMPLETED,
        kycLevel = KycLevel.LEVEL_1
    )

    val profile = Profile(
        userId = "user-123",
        email = "user@example.com",
        firstName = "firstname",
        lastName = "lastname",
        birthDate = LocalDateTime.now().minusYears(20),
        address = "test",
        mobile = "09120000000",
        postalCode = "1234567890",
        nationality = NationalityType.IRANIAN,
        identifier = "1234567890",
        gender = Gender.MALE,
        status = ProfileStatus.SYSTEM_APPROVED,
        createDate = LocalDateTime.now(),
        lastUpdateDate = LocalDateTime.now(),
        creator = "test-suite",
        personalIdentityMatch = true,
        mobileIdentityMatch = true,
    )

    val iranianRequest = CompleteProfileRequest(
        firstName = "firstname",
        lastName = "lastname",
        nationality = NationalityType.IRANIAN,
        identifier = "1234567890",
        gender = Gender.MALE,
        birthDate = 1025292600000
        )
    val foreignRequest = iranianRequest.copy(nationality = NationalityType.NON_IRANIAN)

    val profileApprovalRequest = ProfileApprovalRequest(1, ProfileApprovalRequestStatus.PENDING)

}