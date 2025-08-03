package co.nilin.opex.profile.ports.postgres.utils

import co.nilin.opex.profile.core.data.profile.CompleteProfileRequest
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import com.google.gson.Gson
import reactor.core.publisher.Mono
import java.time.LocalDateTime

fun <T> Any.convert(classOfT: Class<T>): T = Gson().fromJson(Gson().toJson(this), classOfT)

fun <T> Mono<Any>.convert(classOfT: Class<T>): Mono<T> = Mono.just(Gson().fromJson(Gson().toJson(this), classOfT))

fun CompleteProfileRequest.toProfileModel(
    existing: ProfileModel,
    mobileMatch: Boolean,
    personalMatch: Boolean
): ProfileModel {
    return ProfileModel(id = existing.id).apply {
        userId = existing.userId
        email = existing.email
        mobile = existing.mobile
        status = existing.status
        createDate = existing.createDate
        creator = existing.creator
        kycLevel = existing.kycLevel
        lastUpdateDate = LocalDateTime.now()

        firstName = this@toProfileModel.firstName
        lastName = this@toProfileModel.lastName
        address = this@toProfileModel.address
        telephone = this@toProfileModel.telephone
        postalCode = this@toProfileModel.postalCode
        nationality = this@toProfileModel.nationality
        identifier = this@toProfileModel.identifier
        gender = this@toProfileModel.gender
        birthDate = this@toProfileModel.birthDate.asLocalDateTime()

        mobileIdentityMatch = mobileMatch
        personalIdentityMatch = personalMatch
    }
}
