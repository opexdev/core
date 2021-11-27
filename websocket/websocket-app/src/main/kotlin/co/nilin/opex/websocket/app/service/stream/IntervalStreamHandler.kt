package co.nilin.opex.websocket.app.service.stream

import co.nilin.opex.websocket.app.config.AppDispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor

abstract class IntervalStreamHandler<T>(
    protected val template: SimpMessagingTemplate,
    private val userRegistry: SimpUserRegistry
) {

    private val streamJobs = hashMapOf<T, StreamJob>()
    private val jobs = hashMapOf<T, ScheduledFuture<*>?>()
    private val intervalExecutor = ScheduledThreadPoolExecutor(1)
    private val governorExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val logger = LoggerFactory.getLogger(IntervalStreamHandler::class.java)

    init {
        intervalExecutor.removeOnCancelPolicy = true
    }

    fun newSubscription(type: T) {
        registerJob(type)
        logger.info("New subscription added for $type")
    }

    private fun registerJob(type: T) {
        runGovernor {
            if (streamJobs[type] == null)
                streamJobs[type] = createJob(type)

            val job = streamJobs[type] ?: return@runGovernor
            if (jobs[type] == null || jobs[type]?.isCancelled == true) {
                logger.info("job running for $type")
                job.isRunAllowed = true
                jobs[type] = intervalExecutor.scheduleAtFixedRate(
                    { job.run(type) },
                    0,
                    job.interval,
                    job.timeUnit
                )
            }
        }
    }

    private fun StreamJob.run(type: T) {
        runBlocking(AppDispatchers.websocketExecutor) {
            if (isRunAllowed) {
                val data = runnable()
                template.convertAndSend(getPath(type), data)
            }
        }
    }

    @Scheduled(fixedDelay = 60 * 1000)
    private fun govern() {
        runGovernor {
            cancelOrphanJobs()
            intervalExecutor.purge()
        }
    }

    private fun cancelOrphanJobs() {
        jobs.entries.forEach { j ->
            val job = j.value
            val count = userRegistry.findSubscriptions { it.destination == getPath(j.key) }.count()
            val sJob = streamJobs[j.key]
            if (count == 0) {
                if (sJob?.isRunAllowed == true){
                    logger.info("No subscriber for ${j.key}. stopping task")
                    sJob.isRunAllowed = false
                }

                if (job?.isCancelled == false)
                    job.cancel(false)
            } else {
                if (job == null || job.isCancelled || job.isDone) {
                    sJob?.let { s ->
                        s.isRunAllowed = true
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

    @Scheduled(initialDelay = 1000, fixedDelay = 60 * 1000)
    private fun logStatus() {
        var runningJob = 0
        jobs.entries.forEach {
            if (it.value != null && (it.value?.isDone == false || it.value?.isCancelled == false)) {
                runningJob++
                logger.info("+ running: ${it.key}")
            }
        }

        logger.info("== Total of $runningJob jobs running ==")
    }

    private fun runGovernor(runnable: suspend () -> Unit) {
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