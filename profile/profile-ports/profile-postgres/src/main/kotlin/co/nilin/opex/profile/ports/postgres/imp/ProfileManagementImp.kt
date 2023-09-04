package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.kyc.core.data.KycLevelDetail
import co.nilin.opex.kyc.core.data.ManualUpdateRequest
import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.core.data.limitation.LimitationReason
import co.nilin.opex.profile.core.data.limitation.LimitationUpdateType
import co.nilin.opex.profile.core.data.limitation.UpdateLimitationRequest
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.ProfilePersister
import co.nilin.opex.profile.ports.postgres.dao.ProfileHistoryRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import co.nilin.opex.profile.core.utils.convert
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
import co.nilin.opex.profile.core.utils.compare
import co.nilin.opex.profile.ports.kyc.imp.KycProxyImp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirst
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class ProfileManagementImp(private var profileRepository: ProfileRepository,
                           private var profileHistoryRepository: ProfileHistoryRepository,
                           private var limitationManagementImp: LimitationManagementImp,
                           private var kycProxyImp: KycProxyImp) : ProfilePersister {
    private val logger = LoggerFactory.getLogger(ProfileManagementImp::class.java)

    @Transactional
    override suspend fun updateProfile(id: String, data: UpdateProfileRequest): Mono<Profile> {
        var newKycLevel: KycLevel? = null
        return profileRepository.findByUserId(id)?.awaitFirstOrNull()?.let { it ->
            with(data) {

                if (isMajorChanges(it, this)) {
                    newKycLevel = applyMajorChangesRequirements(it, this)
                }
                if (isContactChanges(it, this))
                    newKycLevel = applyContactChangesRequirements(it, this)
            }
            var newProfileModel = data.convert(ProfileModel::class.java)

            newProfileModel.id = it.id
            newProfileModel.kycLevel = it.kycLevel
            newProfileModel.userId = it.userId
            newProfileModel.email = it.email
            newProfileModel.status = it.status
            newProfileModel.createDate = it.createDate
            newProfileModel.lastUpdateDate = LocalDateTime.now()

            // 1.new kyc level was sent to kyc module
            // 2.kyc module as soon as possible will push that message into all module includes profile
            // 3. we return new kyc level to user locally and based on changes of close future in database

            profileRepository.save(newProfileModel).map { convert(Profile::class.java) }.map { d-> newKycLevel.let { d.kycLevel=newKycLevel}
                d }


        } ?: throw OpexException(OpexError.UserNotFound)
    }

    //todo
    //update shared fields in keycloak
    override suspend fun updateProfileAsAdmin(id: String, data: Profile): Mono<Profile> {

        return profileRepository.findByUserId(id)?.awaitFirstOrNull()?.let {
            with(data) {
                this.lastUpdateDate = java.time.LocalDateTime.now()
                this.createDate = createDate
                this.kycLevel = kycLevel
                this.email = email
                this.userId = userId
            }
            var newProfileModel = data.convert(ProfileModel::class.java)
            newProfileModel.id = it.id
            profileRepository.save(newProfileModel).map { convert(Profile::class.java) }
        } ?: throw OpexException(OpexError.UserNotFound)
    }


    override suspend fun createProfile(data: Profile): Mono<Profile> {
        profileRepository.findByUserIdOrEmail(data.userId!!, data.email!!)?.awaitFirstOrNull()?.let {
            throw OpexException(OpexError.UserIdAlreadyExists)
        } ?: run {
            val profile: ProfileModel = data.convert(ProfileModel::class.java)
            profileRepository.save(profile).awaitFirstOrNull()
            return Mono.just(data)
        }
    }

    override suspend fun getProfile(id: String): Mono<Profile>? {

        return profileRepository.findByUserId(id)?.map {
            it.convert(Profile::class.java)
        } ?: throw OpexException(OpexError.UserNotFound)

    }

    override suspend fun getAllProfile(offset: Int, size: Int, profileRequest: ProfileRequest): Flow<Profile>? {
        if (profileRequest.partialSearch == false)
            return profileRepository.findUsersBy(profileRequest.userId, profileRequest.mobile,
                    profileRequest.email, profileRequest.firstName, profileRequest.lastName,
                    profileRequest.nationalCode, profileRequest.createDateFrom, profileRequest.createDateTo,
                    PageRequest.of(offset, size, Sort.by(Sort.Direction.ASC, "id")))?.map { p -> p.convert(Profile::class.java) }
        else {
            return profileRepository.searchUsersBy(profileRequest.userId, profileRequest.mobile,
                    profileRequest.email, profileRequest.firstName, profileRequest.lastName,
                    profileRequest.nationalCode, profileRequest.createDateFrom, profileRequest.createDateTo,
                    PageRequest.of(offset, size, Sort.by(Sort.Direction.ASC, "id")))?.map { p -> p.convert(Profile::class.java) }
        }
    }

    override suspend fun getHistory(userId: String, offset: Int, size: Int): List<ProfileHistory> {
        val resp: MutableList<ProfileHistory> = ArrayList()

        profileRepository.findByUserId(userId)?.awaitFirstOrNull() ?: throw OpexException(OpexError.UserNotFound)
        profileHistoryRepository.findByUserId(userId, PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "changeRequestDate")))
                .map { p ->
                    p.convert(ProfileHistory::class.java)
                }
                .toList()
                .windowed(2, 1, true)
                .forEach { window: List<ProfileHistory> ->
                    val new = window.first()
                    val past = window.last()
                    if (past.userId?.isNotBlank() == true) {
                        new.updatedItem = new.compare(past)
                        resp.add(new)
                    } else
                        resp.add(past)
                }

        return resp.toList()
    }

    override suspend fun updateUserLevel(userId: String, userLevel: KycLevel) {
        profileRepository.findByUserId(userId)?.block()?.let { profileModel ->
            profileModel.kycLevel = userLevel
            profileRepository.save(profileModel).awaitFirstOrNull()

        } ?: throw OpexException(OpexError.UserNotFound)
    }


    fun isMajorChanges(oldData: ProfileModel, newData: UpdateProfileRequest): Boolean {
        return !oldData.firstName.equals(newData.firstName) || !oldData.lastName.equals(newData.lastName)
    }

    fun isContactChanges(oldData: ProfileModel, newData: UpdateProfileRequest): Boolean {
        // return oldData.email != newData.email ||
        return !oldData.mobile.equals(newData.mobile)
    }

    suspend fun applyMajorChangesRequirements(oldData: ProfileModel, newData: UpdateProfileRequest): KycLevel? {
        //todo
        //read from panel
        val newKycLevel = KycLevel.Level1

        updateKycLevel(userId = oldData.userId!!, kycLevel = newKycLevel, LimitationReason.MajorProfileChange.name)
        limitationManagementImp.updateLimitation(UpdateLimitationRequest(oldData.userId, arrayOf(

                ActionType.CashOut, ActionType.Withdraw).asList(), null, LimitationUpdateType.Revoke, null, null, LimitationReason.MajorProfileChange))

        return newKycLevel
    }

    suspend fun applyContactChangesRequirements(oldData: ProfileModel, newData: UpdateProfileRequest): KycLevel? {
        //todo
        //read from panel
        val newKycLevel = KycLevel.Level1

        updateKycLevel(userId = oldData.userId!!, kycLevel = newKycLevel, LimitationReason.MajorProfileChange.name)
        limitationManagementImp.updateLimitation(UpdateLimitationRequest(oldData.userId, arrayOf(

                ActionType.Withdraw).asList(), null, LimitationUpdateType.Revoke, null, null, LimitationReason.ContactProfileChange))
        return newKycLevel
    }


    suspend fun updateKycLevel(userId: String, kycLevel: KycLevel, reason: String?) {
        val kycLevelDetail = if (kycLevel == KycLevel.Level1) KycLevelDetail.ManualUpdateLevel1 else KycLevelDetail.ManualUpdateLevel2
        kycProxyImp.updateKycLevel(ManualUpdateRequest(kycLevelDetail).apply {
            this.userId = userId
            this.description = reason
        })
    }
}