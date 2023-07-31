package co.nilin.opex.kyc.ports.postgres.imp

import co.nilin.opex.core.event.KycLevelUpdatedEvent
import co.nilin.opex.core.spi.KycLevelUpdatedPublisher
import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import org.aspectj.lang.annotation.Around
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserLevelAspect (val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher){
    @Around("execution(* co.nilin.opex.kyc.ports.postgres.dao.UserStatusRepository.save(..))")
    @Throws(Throwable::class)
   suspend fun saveAspect(userStatusModel: UserStatusModel){

        kycLevelUpdatedPublisher.publish(KycLevelUpdatedEvent(userId = userStatusModel.userId, kycLevel = userStatusModel.kycLevel.kycLevel, LocalDateTime.now()))
    }
}