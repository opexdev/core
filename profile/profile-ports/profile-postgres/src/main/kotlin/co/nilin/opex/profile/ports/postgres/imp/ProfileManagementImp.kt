package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
import co.nilin.opex.profile.core.data.profile.RequiredAdminActions
import co.nilin.opex.profile.core.spi.ProfilePersister
import co.nilin.opex.profile.ports.postgres.dao.ProfileHistoryRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import co.nilin.opex.profile.ports.postgres.utils.convert
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileManagementImp(private var profileRepository: ProfileRepository,
                           private var profileHistoryRepository: ProfileHistoryRepository) : ProfilePersister {
    private val logger = LoggerFactory.getLogger(ProfileManagementImp::class.java)

    @Transactional
    override suspend fun updateProfile(id: String, data: Profile): Profile {

        return profileRepository.findByUserId(id)?.awaitFirstOrNull()?.let {
            with(data) {
                this.status = status
                this.lastUpdateDate = java.time.LocalDateTime.now()
                this.createDate = createDate
                this.email = email
                this.userId = id
                if (isMajorChanges(it,this)) {
                    this.requiredAdminActions= RequiredAdminActions.MacjoreChangesReview
                   // macjoreChangesRequirements()
                }
                if (isContactChanches(it,this))
                    this.requiredAdminActions= RequiredAdminActions.ContactChangesReview
                  //  contactChangesRequirements()
            }
            var newProfileModel = data.convert(ProfileModel::class.java)
            newProfileModel.id = it.id
            profileRepository.save(newProfileModel).awaitFirstOrNull()!!.convert(Profile::class.java)
        } ?: throw OpexException(OpexError.UserNotFound)
    }

    override suspend fun updateProfileAsAdmin(id: String, data: Profile): Profile {

        return profileRepository.findByUserId(id)?.awaitFirstOrNull()?.let {
            with(data) {
                this.lastUpdateDate = java.time.LocalDateTime.now()
                this.createDate = createDate
            }
            var newProfileModel = data.convert(ProfileModel::class.java)
            newProfileModel.id = it.id
            profileRepository.save(newProfileModel).awaitFirstOrNull()!!.convert(Profile::class.java)
        } ?: throw OpexException(OpexError.UserNotFound)
    }


    override suspend fun createProfile(data: Profile): Profile {
        profileRepository.findByUserIdOrEmail(data.userId!!, data.email!!)?.awaitFirstOrNull()?.let {
            throw OpexException(OpexError.UserIdAlreadyExists)
        } ?: run {
            val profile: ProfileModel = data.convert(ProfileModel::class.java)
            profileRepository.save(profile).awaitFirstOrNull()
            return data
        }
    }

    override suspend fun getProfile(id: String): Profile? {

        return profileRepository.findByUserId(id)?.awaitFirstOrNull()?.let {
            it.convert(Profile::class.java)
        } ?: throw OpexException(OpexError.UserNotFound)

    }

    override suspend fun getAllProfile(offset: Int, size: Int): List<Profile> {
        return profileRepository.findBy(PageRequest.of(offset, size, Sort.by(Sort.Direction.ASC, "id"))).map { p -> p.convert(Profile::class.java) }.toList()
    }

    override suspend fun getHistory(userId: String, offset: Int, size: Int): List<ProfileHistory> {
        profileRepository.findByUserId(userId)?.awaitFirstOrNull() ?: throw OpexException(OpexError.UserNotFound)
        return profileHistoryRepository.findByUserId(userId, PageRequest.of(offset, size, Sort.by(Sort.Direction.ASC, "id"))).map { p -> p.convert(ProfileHistory::class.java) }.toList()

    }

    fun isMajorChanges(oldData: ProfileModel, newData: Profile): Boolean {
        return !oldData.firstName.equals(newData.firstName) || !oldData.firstName.equals(newData.firstName)
    }

    fun isContactChanches(oldData: ProfileModel, newData: Profile): Boolean {
        return !oldData.email.equals(newData.email) || !oldData.mobile.equals(newData.mobile)
    }

}