package co.nilin.opex.profile.app.service


import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
import co.nilin.opex.profile.core.spi.ProfilePersister
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import co.nilin.opex.profile.core.data.profile.KycLevel
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Component
class ProfileManagement(
        private val profilePersister: ProfilePersister
) {
    private val logger = LoggerFactory.getLogger(ProfileManagement::class.java)
    suspend fun registerNewUser(event: UserCreatedEvent) {
        with(event) {
            profilePersister.createProfile(Profile(firstName = firstName, lastName = lastName, email = email,
                    userId = uuid, status = co.nilin.opex.profile.core.data.profile.UserStatus.Active, createDate = LocalDateTime.now(), lastUpdateDate = LocalDateTime.now(), creator = "system"))
        }
    }

    suspend fun getAllProfiles(offset: Int, size: Int): List<Profile> {

        return profilePersister.getAllProfile(offset, size)
    }

    suspend fun getProfile(userId: String): Profile? {
        return profilePersister.getProfile(userId)
    }

    suspend fun update(userId: String, newProfile: Profile): Profile? {
        return profilePersister.updateProfile(userId, newProfile)
    }
    suspend fun updateAsAdmin(userId: String, newProfile: Profile): Profile? {
        return profilePersister.updateProfile(userId, newProfile)
    }

    suspend fun create(userId: String, newProfile: Profile): Profile? {
        newProfile.userId = userId
        return profilePersister.createProfile(newProfile)
    }
    suspend fun getHistory(userId: String,offset:Int,size:Int): List<ProfileHistory>? {
        return profilePersister.getHistory(userId,offset,size)
    }

    suspend fun updateUserLevel(userId: String,userLevel:KycLevel) {
         profilePersister.updateUserLevel(userId,userLevel)
    }
}



