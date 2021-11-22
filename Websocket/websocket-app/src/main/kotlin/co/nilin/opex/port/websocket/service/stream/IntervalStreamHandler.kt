package co.nilin.opex.port.websocket.service.stream

import co.nilin.opex.port.websocket.config.AppDispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture

abstract class IntervalStreamHandler<T>(
    protected val template: SimpMessagingTemplate,
    protected val userRegistry: SimpUserRegistry
) {

    private val streamJobs = hashMapOf<T, StreamJob>()
    private val jobs = hashMapOf<T, ScheduledFuture<*>?>()
    private val intervalExecutor = Executors.newSingleThreadScheduledExecutor()
    private val governorExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val logger = LoggerFactory.getLogger(IntervalStreamHandler::class.java)

    fun newSubscription(type: T) {
        registerJob(type, createJob(type))
        logger.info("New subscription added for $type")
    }

    private fun registerJob(type: T, job: StreamJob) {
        streamJobs[type] = job
        runGovernor {
            jobs[type] = intervalExecutor.scheduleAtFixedRate(
                { job.run(type) },
                0,
                job.interval,
                job.timeUnit
            )
        }
    }

    private fun StreamJob.run(type: T) {
        logger.info("job running for $type")
        runBlocking(AppDispatchers.websocketExecutor) {
            val data = runnable()
            template.convertAndSend(getPath(type), data)
        }
    }

    @Scheduled(fixedDelay = 60 * 1000)
    private fun govern() {
        runGovernor {
            jobs.entries.forEach { j ->
                val job = j.value
                val count = userRegistry.findSubscriptions { it.destination == getPath(j.key) }.count()
                if (count == 0) {
                    logger.info("No subscriber for ${j.key}. task stopped")
                    if (job?.isCancelled == false)
                        job.cancel(false)
                } else {
                    if (job?.isCancelled == true) {
                        streamJobs[j.key]?.let { s ->
                            jobs[j.key] = intervalExecutor.scheduleAtFixedRate(
                                { s.run(j.key) },
                                0,
                                s.interval,
                                s.timeUnit
                            )
                        }
                        logger.info("Starting job")
                    }
                }
            }
        }
    }

    private fun runGovernor(runnable: () -> Unit) {
        runBlocking(governorExecutor) { runnable() }
    }

    protected fun hasSubscription(): Boolean {
        return userRegistry.users.isNotEmpty()
    }

    protected fun hasSubscriptionFor(type: T): Boolean {
        return userRegistry.findSubscriptions { it.destination == getPath(type) }.isNotEmpty()
    }

    protected fun hasSubscriptionForAny(vararg paths: String): Boolean {
        return userRegistry.findSubscriptions { paths.contains(it.destination) }.isNotEmpty()
    }

    protected fun hasSession(): Boolean {
        return userRegistry.users.map { it.sessions }.flatten().isNotEmpty()
    }

    protected abstract fun createJob(type: T): StreamJob

    protected abstract fun getPath(type: T): String

}