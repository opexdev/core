package co.nilin.opex.profile.ports.postgres.sample

import co.nilin.opex.profile.core.data.profile.Gender
import co.nilin.opex.profile.core.data.profile.NationalityType
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileStatus
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import java.time.LocalDateTime

object VALID {

    val profileModelWithoutEmail = ProfileModel(id = 1).apply {
        userId = "user-123"
        mobile = "0912000000"
        status = ProfileStatus.CREATED
        createDate = LocalDateTime.now()
        creator = "test-suite"
    }
    val profileModelWithoutMobile = ProfileModel(id = 1).apply {
        userId = "user-123"
        email = "user@example.com"
        status = ProfileStatus.CREATED
        createDate = LocalDateTime.now()
        creator = "test-suite"
    }

    val profileModel = ProfileModel(id = 1).apply {
        userId = "user-123"
        email = "user@example.com"
        firstName = "firstname"
        lastName = "lastname"
        birthDate = LocalDateTime.now().minusYears(20)
        address = "test"
        mobile = "09120000000"
        postalCode = "1234567890"
        nationality = NationalityType.IRANIAN
        identifier= "1234567890"
        gender = Gender.MALE
        status = ProfileStatus.SYSTEM_APPROVED
        createDate = LocalDateTime.now()
        lastUpdateDate = LocalDateTime.now()
        creator = "test-suite"
        personalIdentityMatch = true
        mobileIdentityMatch = true
    }

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
        identifier= "1234567890",
        gender = Gender.MALE,
        status = ProfileStatus.SYSTEM_APPROVED,
        createDate = LocalDateTime.now(),
        lastUpdateDate = LocalDateTime.now(),
        creator = "test-suite",
        personalIdentityMatch = true,
        mobileIdentityMatch = true,
    )

}