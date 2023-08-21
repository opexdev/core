package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.kyc.core.data.KycLevelDetail
import co.nilin.opex.kyc.core.data.ManualUpdateRequest
import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.core.data.limitation.LimitationReason
import co.nilin.opex.profile.core.data.limitation.LimitationUpdateType
import co.nilin.opex.profile.core.data.limitation.UpdateLimitationRequest
import co.nilin.opex.profile.core.data.profile.KycLevel
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
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
import kotlinx.coroutines.reactive.awaitFirst

@Service
class ProfileManagementImp(private var profileRepository: ProfileRepository,
                           private var profileHistoryRepository: ProfileHistoryRepository,
                           private var limitationManagementImp: LimitationManagementImp,
                           private var kycProxyImp: KycProxyImp) : ProfilePersister {
    private val logger = LoggerFactory.getLogger(ProfileManagementImp::class.java)

    @Transactional
    override suspend fun updateProfile(id: String, data: Profile): Profile {
        var newKycLevel: KycLevel? = null
        return profileRepository.findByUserId(id)?.awaitFirstOrNull()?.let { it ->
            with(data) {
                this.status = status
                this.lastUpdateDate = java.time.LocalDateTime.now()
                this.createDate = createDate
                this.email = email
                this.userId = id
                if (isMajorChanges(it, this)) {
                    newKycLevel = applyMajorChangesRequirements(it, this)
                }
                if (isContactChanges(it, this))
                    newKycLevel = applyContactChangesRequirements(it, this)
            }
            var newProfileModel = data.convert(ProfileModel::class.java)
            newProfileModel.id = it.id
            newKycLevel?.let { kl -> newProfileModel.kycLevel = kl }
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


    fun isMajorChanges(oldData: ProfileModel, newData: Profile): Boolean {
        return !oldData.firstName.equals(newData.firstName) || !oldData.firstName.equals(newData.firstName)
    }

    fun isContactChanges(oldData: ProfileModel, newData: Profile): Boolean {
        return oldData.email != newData.email || !oldData.mobile.equals(newData.mobile)
    }

    suspend fun applyMajorChangesRequirements(oldData: ProfileModel, newData: Profile): KycLevel? {
        //todo
        //read from panel
        val newKycLevel = KycLevel.Level1

        updateKycLevel(userId = newData.userId!!, kycLevel = newKycLevel, LimitationReason.MajorProfileChange.name)
        limitationManagementImp.updateLimitation(UpdateLimitationRequest(oldData.userId, arrayOf(

                ActionType.CashOut, ActionType.Withdraw).asList(), null, LimitationUpdateType.Revoke, null, null, LimitationReason.MajorProfileChange))

        return newKycLevel
    }

    suspend fun applyContactChangesRequirements(oldData: ProfileModel, newData: Profile): KycLevel? {
        //todo
        //read from panel
        val newKycLevel = KycLevel.Level1

        updateKycLevel(userId = newData.userId!!, kycLevel = newKycLevel, LimitationReason.MajorProfileChange.name)
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