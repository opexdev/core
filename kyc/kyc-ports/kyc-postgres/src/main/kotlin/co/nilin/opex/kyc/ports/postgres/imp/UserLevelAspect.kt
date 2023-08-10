package co.nilin.opex.kyc.ports.postgres.imp

import co.nilin.opex.kyc.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.kyc.core.spi.KycLevelUpdatedPublisher
import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import org.aspectj.lang.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Configuration
@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class UserLevelAspect(val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher) {

    private val logger = LoggerFactory.getLogger(UserLevelAspect::class.java)


   @Pointcut("execution(* UserStatusRepository.save(..)" )// the pointcut expression
    suspend fun saveAspect(userStatusModel: UserStatusModel) {
        logger.info("============================going to call after save function")
        kycLevelUpdatedPublisher.publish(KycLevelUpdatedEvent(userId = userStatusModel.userId, kycLevel = userStatusModel.kycLevel.kycLevel, LocalDateTime.now()))
    }
}