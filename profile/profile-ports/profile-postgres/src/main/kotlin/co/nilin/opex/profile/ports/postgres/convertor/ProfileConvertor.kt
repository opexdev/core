package co.nilin.opex.profile.ports.postgres.convertor

import co.nilin.opex.profile.core.data.profile.CompleteProfileResponse
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel

fun convertProfileModelToCompleteProfileResponse(profileModel: ProfileModel): CompleteProfileResponse {
    return CompleteProfileResponse(
        id = profileModel.id,
        email = profileModel.email,
        userId = profileModel.userId,
        firstName = profileModel.firstName,
        lastName = profileModel.lastName,
        address = profileModel.address,
        mobile = profileModel.mobile,
        telephone = profileModel.telephone,
        postalCode = profileModel.postalCode,
        nationality = profileModel.nationality,
        identifier = profileModel.identifier,
        gender = profileModel.gender,
        birthDate = profileModel.birthDate,
        status = profileModel.status,
        kycLevel = profileModel.kycLevel,
        verificationStatus = profileModel.verificationStatus
    )
}
