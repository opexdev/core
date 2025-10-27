package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.kyc.KycLevelDetail
import co.nilin.opex.profile.core.data.kyc.ManualUpdateRequest
import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.core.data.limitation.LimitationReason
import co.nilin.opex.profile.core.data.limitation.LimitationUpdateType
import co.nilin.opex.profile.core.data.limitation.UpdateLimitationRequest
import co.nilin.opex.profile.core.data.profile.*
import co.nilin.opex.profile.core.spi.ProfilePersister
import co.nilin.opex.profile.core.utils.compare
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.profile.ports.kyc.imp.KycProxyImp
import co.nilin.opex.profile.ports.postgres.dao.ProfileHistoryRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import co.nilin.opex.profile.ports.postgres.utils.toProfileModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class ProfileManagementImp(
    private var profileRepository: ProfileRepository,
    private var profileHistoryRepository: ProfileHistoryRepository,
    private var limitationManagementImp: LimitationManagementImp,
    private var kycProxyImp: KycProxyImp,
) : ProfilePersister {
    private val logger = LoggerFactory.getLogger(ProfileManagementImp::class.java)
    private val EmailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private val MobileRegex = Regex("^09\\d{9}$")

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

            profileRepository.save(newProfileModel).map { convert(Profile::class.java) }.map { d ->
                newKycLevel.let { d.kycLevel = newKycLevel }
                d
            }


        } ?: throw OpexError.UserNotFound.exception()
    }

    override suspend fun completeProfile(
        id: String,
        data: CompleteProfileRequest,
        mobileIdentityMatch: Boolean?,
        personalIdentityMatch: Boolean?
    ): Mono<Profile> {
        val existingProfile = profileRepository.findByUserId(id)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()

        val newProfileModel = data.toProfileModel(
            existing = existingProfile,
            mobileMatch = mobileIdentityMatch,
            personalMatch = personalIdentityMatch
        )

        return profileRepository.save(newProfileModel).map {
            it.convert(Profile::class.java)
        }
    }

    //todo
    //update shared fields in keycloak
    override suspend fun updateProfileAsAdmin(id: String, data: Profile): Mono<Profile> {

        return profileRepository.findByUserId(id)?.awaitFirstOrNull()?.let {
            with(data) {
                this.lastUpdateDate = LocalDateTime.now()
                this.createDate = createDate
                this.kycLevel = kycLevel
                this.email = email
                this.userId = userId
            }
            var newProfileModel = data.convert(ProfileModel::class.java)
            newProfileModel.id = it.id
            profileRepository.save(newProfileModel).map { convert(Profile::class.java) }
        } ?: throw OpexError.UserNotFound.exception()
    }

    override suspend fun createProfile(data: Profile): Mono<Profile> {
        if (data.email.isNullOrBlank() && data.mobile.isNullOrBlank()) {
            throw OpexError.BadRequest.exception("email and mobile is null or empty")
        }
        profileRepository.findByUserIdOrEmailOrMobile(data.userId!!, data.email, data.mobile)?.awaitFirstOrNull()?.let {
            throw OpexError.UserIdAlreadyExists.exception()
        }
        val profile: ProfileModel = data.convert(ProfileModel::class.java)
        val saved = profileRepository.save(profile)
            .awaitFirstOrNull() ?: throw OpexError.BadRequest.exception("Failed to save profile")
        return Mono.just(saved.convert(Profile::class.java))
    }

    override suspend fun getProfile(userId: String): Mono<Profile>? {

        return profileRepository.findByUserId(userId)?.map {
            it.convert(Profile::class.java)
        } ?: throw OpexError.UserNotFound.exception()

    }

    override suspend fun getProfileId(userId: String): Long {

        return profileRepository.findByUserId(userId)?.awaitFirst()?.id ?: throw OpexError.ProfileNotfound.exception()

    }

    override suspend fun getProfile(id: Long): Mono<Profile> {
        val profile: Profile =
            profileRepository.findById(id).awaitFirstOrNull()?.convert(Profile::class.java)
                ?: throw OpexError.ProfileNotfound.exception()
        return Mono.just(profile)
    }

    override suspend fun getAllProfile(offset: Int, size: Int, profileRequest: ProfileRequest): Flow<Profile>? {
        if (profileRequest.partialSearch == false)
            return profileRepository.findUsersBy(
                profileRequest.userId, profileRequest.mobile,
                profileRequest.email, profileRequest.firstName, profileRequest.lastName,
                profileRequest.nationalCode, profileRequest.createDateFrom, profileRequest.createDateTo,
                PageRequest.of(offset, size, Sort.by(Sort.Direction.ASC, "id"))
            )?.map { p -> p.convert(Profile::class.java) }
        else {
            return profileRepository.searchUsersBy(
                profileRequest.userId, profileRequest.mobile,
                profileRequest.email, profileRequest.firstName, profileRequest.lastName,
                profileRequest.nationalCode, profileRequest.createDateFrom, profileRequest.createDateTo,
                PageRequest.of(offset, size, Sort.by(Sort.Direction.ASC, "id"))
            )?.map { p -> p.convert(Profile::class.java) }
        }
    }

    override suspend fun getHistory(userId: String, offset: Int, size: Int): List<ProfileHistory> {
        val resp: MutableList<ProfileHistory> = ArrayList()

        profileRepository.findByUserId(userId)?.awaitFirstOrNull() ?: throw OpexError.UserNotFound.exception()
        profileHistoryRepository.findByUserId(
            userId,
            PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "changeRequestDate"))
        )
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

    override suspend fun updateUserLevelAndStatus(userId: String, userLevel: KycLevel) {
        profileRepository.findByUserId(userId)?.awaitFirstOrNull()?.let { profileModel ->
            profileModel.kycLevel = userLevel
            profileRepository.save(profileModel).awaitFirstOrNull()

        } ?: throw OpexError.UserNotFound.exception()
    }

    override suspend fun validateEmailForUpdate(userId: String, email: String) {
        validateEmailFormat(email)

        val profile = profileRepository.findByUserId(userId)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()

        if (!profile.email.isNullOrEmpty())
            throw OpexError.EmailAlreadySet.exception()
    }

    override suspend fun validateMobileForUpdate(userId: String, mobile: String) {
        validateMobileFormat(mobile)

        val profile = profileRepository.findByUserId(userId)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()

        if (!profile.mobile.isNullOrEmpty())
            throw OpexError.MobileAlreadySet.exception()
    }

    override suspend fun updateMobile(userId: String, mobile: String) {
        if (profileRepository.findByMobile(mobile)?.awaitFirstOrNull() != null)
            throw OpexError.MobileAlreadyExists.exception()
        val profile = profileRepository.findByUserId(userId)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()
        profile.mobile = mobile
        profile.status = ProfileStatus.CONTACT_INFO_COMPLETED
        profileRepository.save(profile).awaitFirstOrNull()
    }

    override suspend fun updateEmail(userId: String, email: String) {
        if (profileRepository.findByEmail(email)?.awaitFirstOrNull() != null)
            throw OpexError.EmailAlreadyExists.exception()
        val profile = profileRepository.findByUserId(userId)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()
        profile.email = email
        profile.status = ProfileStatus.CONTACT_INFO_COMPLETED
        profileRepository.save(profile).awaitFirstOrNull()
    }

    override suspend fun updateStatus(
        userId: String,
        status: ProfileStatus
    ) {
        val profile = profileRepository.findByUserId(userId)?.awaitFirstOrNull()
            ?: throw OpexError.ProfileNotfound.exception()
        profile.status = status
        profileRepository.save(profile).awaitFirstOrNull()
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
        val newKycLevel = KycLevel.LEVEL_1

        updateKycLevel(userId = oldData.userId!!, kycLevel = newKycLevel, LimitationReason.MajorProfileChange.name)
        limitationManagementImp.updateLimitation(
            UpdateLimitationRequest(
                oldData.userId, arrayOf(

                    ActionType.CashOut, ActionType.Withdraw
                ).asList(), null, LimitationUpdateType.Revoke, null, null, LimitationReason.MajorProfileChange
            )
        )

        return newKycLevel
    }

    suspend fun applyContactChangesRequirements(oldData: ProfileModel, newData: UpdateProfileRequest): KycLevel? {
        //todo
        //read from panel
        val newKycLevel = KycLevel.LEVEL_1

        updateKycLevel(userId = oldData.userId!!, kycLevel = newKycLevel, LimitationReason.MajorProfileChange.name)
        limitationManagementImp.updateLimitation(
            UpdateLimitationRequest(
                oldData.userId, arrayOf(

                    ActionType.Withdraw
                ).asList(), null, LimitationUpdateType.Revoke, null, null, LimitationReason.ContactProfileChange
            )
        )
        return newKycLevel
    }

    suspend fun updateKycLevel(userId: String, kycLevel: KycLevel, reason: String?) {
        val kycLevelDetail =
            if (kycLevel == KycLevel.LEVEL_1) KycLevelDetail.ManualUpdateLevel1 else KycLevelDetail.ManualUpdateLevel3
        kycProxyImp.updateKycLevel(ManualUpdateRequest(kycLevelDetail).apply {
            this.userId = userId
            this.description = reason
        })
    }

    private fun validateEmailFormat(email: String) {
        if (!EmailRegex.matches(email)) throw OpexError.InvalidEmail.exception()
    }

    private fun validateMobileFormat(mobile: String) {
        if (!MobileRegex.matches(mobile)) throw OpexError.InvalidMobile.exception()
    }
}