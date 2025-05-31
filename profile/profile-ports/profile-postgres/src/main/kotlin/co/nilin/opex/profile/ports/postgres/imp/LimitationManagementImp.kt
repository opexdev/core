package co.nilin.opex.profile.ports.postgres.imp

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.core.data.limitation.*
import co.nilin.opex.profile.core.spi.LimitationPersister
import co.nilin.opex.profile.ports.postgres.dao.LimitationHistoryRepository
import co.nilin.opex.profile.ports.postgres.dao.ProfileRepository
import co.nilin.opex.profile.ports.postgres.dao.LimitationRepository
import co.nilin.opex.profile.ports.postgres.model.entity.LimitationModel
import co.nilin.opex.profile.core.utils.convert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LimitationManagementImp(
    private var limitationRepository: LimitationRepository,
    private var profileRepository: ProfileRepository,
    private var limitationHistoryRepository: LimitationHistoryRepository
) : LimitationPersister {
    private val logger = LoggerFactory.getLogger(LimitationManagementImp::class.java)

    @Transactional
    override suspend fun updateLimitation(updatePermissionRequest: UpdateLimitationRequest) {
        var BreakException = {};

        //is there particular user? yes
        updatePermissionRequest.userId?.let {
            profileRepository.findByUserId(updatePermissionRequest.userId!!)?.awaitFirstOrNull()
                ?.let {
                    //set limitations for specific user on some actions
                    logger.info("set limitations for specific user on some actions")
                    updatePermissionRequest.actions?.forEach {
                        if (updatePermissionRequest.updateType == LimitationUpdateType.Revoke) {
                            limitationRepository.findByLimitationOn(updatePermissionRequest.userId + "_$it")
                                ?.awaitFirstOrNull()
                                ?: run {
                                    with(updatePermissionRequest) {
                                        var limit = updatePermissionRequest.convert(LimitationModel::class.java)
                                        limit.actionType = it
                                        limit.limitationOn = userId + "_$it"
                                        limit.createDate = LocalDateTime.now()
                                        limitationRepository.save(limit).awaitFirstOrNull()

                                    }
                                }
                        } else {
                            logger.info("reset limitations for specific user on some actions")
                            //reset limitations for specific user on some actions/all
                            if (updatePermissionRequest.actions?.contains(ActionType.All) == true) {
                                limitationRepository.deleteByUserId(updatePermissionRequest.userId!!).awaitFirstOrNull()
                                //todo break
                            }
                            limitationRepository.deleteByLimitationOn(updatePermissionRequest.userId + "_$it")
                                .awaitFirstOrNull()
                        }
                    }
                } ?: throw OpexError.UserNotFound.exception()
            //is there particular user? no
        } ?: run {
            //set limitations for all users on some actions
            logger.info("set limitations for all users on some actions")
            updatePermissionRequest.actions?.forEach {
                if (updatePermissionRequest.updateType == LimitationUpdateType.Revoke) {
                    limitationRepository.findByLimitationOn("All_$it")?.awaitFirstOrNull()
                        ?: run {
                            with(updatePermissionRequest) {
                                var limit = updatePermissionRequest.convert(LimitationModel::class.java)
                                limit.userId = "All"
                                limit.actionType = it
                                limit.limitationOn = "All_$it"
                                limit.createDate = LocalDateTime.now()
                                limitationRepository.save(limit).awaitFirstOrNull()
                            }
                        }
                } else {
                    //reset limitations for all users on some actions/all
                    logger.info("reset limitations for all users on some actions")
                    if (updatePermissionRequest.actions?.contains(ActionType.All) == true) {
                        limitationRepository.deleteAll().awaitFirstOrNull()
                        //break
                    }
                    limitationRepository.deleteByActionType(it).awaitFirstOrNull()
                }
            }
        }

    }

    override suspend fun getLimitation(
        userId: String?,
        action: ActionType?,
        reason: LimitationReason?,
        offset: Int?,
        size: Int?
    ): Flow<Limitation>? {
        return limitationRepository.findAllLimitation(
            userId,
            action,
            reason,
            offset!!,
            size!!,
            PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "id"))
        )?.map { l -> l.convert(Limitation::class.java) }
    }


    override suspend fun getLimitationHistory(
        userId: String?,
        action: ActionType?,
        reason: LimitationReason?,
        offset: Int,
        size: Int
    ): Flow<LimitationHistory>? {
        return limitationHistoryRepository.findAllLimitationHistory(
            userId,
            action,
            reason,
            offset,
            size,
            PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "id"))
        )?.map { l -> l.convert(LimitationHistory::class.java) }
    }

}