package co.nilin.opex.profile.app.service


import co.nilin.opex.profile.core.spi.ProfilePersister
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.LimitationPersister
import co.nilin.opex.profile.core.spi.LinkedAccountPersister
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class ProfileManagement(
        private val profilePersister: ProfilePersister,
        private val linkedAccountPersister: LinkedAccountPersister,
        private val limitationPersister: LimitationPersister
) {
    private val logger = LoggerFactory.getLogger(ProfileManagement::class.java)
    suspend fun registerNewUser(event: UserCreatedEvent) {
        with(event) {
            profilePersister.createProfile(Profile(firstName = firstName, lastName = lastName, email = email,
                    userId = uuid, status = co.nilin.opex.profile.core.data.profile.UserStatus.Active, createDate = LocalDateTime.now(), lastUpdateDate = LocalDateTime.now(), creator = "system"))
        }
    }

    suspend fun getAllProfiles(offset: Int, size: Int, profileRequest: ProfileRequest): List<Profile?>? {
        profileRequest.accountNumber?.let {
            val res = profilePersister.getAllProfile(offset, size, profileRequest)?.toList()
            val accountOwner = linkedAccountPersister.getOwner(profileRequest.accountNumber!!, profileRequest.partialSearch)
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
}



