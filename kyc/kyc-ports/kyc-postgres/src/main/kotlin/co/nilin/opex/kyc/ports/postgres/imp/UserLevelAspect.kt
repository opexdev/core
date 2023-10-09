package co.nilin.opex.kyc.ports.postgres.imp

import co.nilin.opex.kyc.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.kyc.core.spi.KycLevelUpdatedPublisher
import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.lang.reflect.Type
import java.time.LocalDateTime


@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class UserLevelAspect(private val kycLevelUpdatedPublisher: KycLevelUpdatedPublisher, private val kycManagementImp: KycManagementImp) {
    private val logger = LoggerFactory.getLogger(UserLevelAspect::class.java)
    val scope = CoroutineScope(Dispatchers.IO)

    @Around(
            """
        @annotation(co.nilin.opex.kyc.core.data.annotation.KycLevelUpdated) &&
        execution(reactor.core.publisher.Mono *(..))
        """
    )
    fun updateKycLevel(joinPoint: ProceedingJoinPoint): Mono<UserStatusModel> =

            (joinPoint.proceed() as Mono<UserStatusModel>)
                    .log()
                    .map { result ->

                        scope.launch {
                            kycManagementImp.userLevelHistory(result.userId)?.map {d->logger.info("========${d.kycLevel}")  }
                            if (kycManagementImp.userLevelHistory(result.userId)?.firstOrNull()?.kycLevel != result.kycLevel) {
                                logger.info("Change in kycStatusModel with updated level" )
                                logger.info("==========================================================================")
                                logger.info("going to push kyc level update for :  ${result.userId}")
                                logger.info("==========================================================================")
                                kycLevelUpdatedPublisher.publish(KycLevelUpdatedEvent(result.userId, result.kycLevel.kycLevel, LocalDateTime.now()))
                            }
                            logger.info("Change in kycStatusModel with same level; No need to publish event" )
                        }
                        result
                    }

}