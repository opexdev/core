package co.nilin.opex.device.app.service

import co.nilin.opex.device.core.spi.SessionPersister
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class SessionExpirationScheduler(
    private val sessionPersister: SessionPersister
) {
    private val logger = LoggerFactory.getLogger(SessionExpirationScheduler::class.java)

    @PostConstruct
    fun startScheduler() {
        Flux.interval(Duration.ofMinutes(1))
            .flatMap {
                Mono.fromCallable {
                    runBlocking {
                        sessionPersister.markExpiredSessions()
                    }
                }
            }
            .subscribe(
                { updated -> logger.info("Expired session update completed: $updated items") },
                { err -> logger.error("Failed to update expired sessions", err) }
            )
    }
}
