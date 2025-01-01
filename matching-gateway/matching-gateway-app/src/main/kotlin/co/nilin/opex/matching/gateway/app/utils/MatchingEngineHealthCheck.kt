package co.nilin.opex.matching.gateway.app.utils

import co.nilin.opex.matching.gateway.ports.kafka.submitter.utils.EventSubmitterInfo
import com.ecwid.consul.v1.ConsulClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

@Component
class MatchingEngineHealthCheck(
    private val consulClient: ConsulClient,
    private val eventSubmitterInfo: EventSubmitterInfo
) {

    private val logger = LoggerFactory.getLogger(MatchingEngineHealthCheck::class.java)
    private val mainIndicators = ArrayList<Boolean>()
    private val lastOrderTime = ArrayList<LocalDateTime>()

    final var failedQueriesInRow = 0
        private set

    final var isAnyEnginesBehind = false
        private set

    final var isAnyEnginesUnhealthy = false
        private set

    private fun fetchMainIndicator(engineId: String): Boolean {
        return String(
            Base64.getDecoder().decode(consulClient.getKVValue("/health/matching-engine-$engineId/isUp").value.value)
        ).toBoolean()
    }

    private fun fetchLastOrderRequestTime(engineId: String): LocalDateTime {
        val value = consulClient.getKVValue("/health/matching-engine-$engineId/lastOrderTime").value.value
        val valDecoded = String(Base64.getDecoder().decode(value))
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(valDecoded.toLong()), ZoneId.systemDefault())
    }

    @Scheduled(initialDelay = 5_000, fixedDelay = 30_000)
    private fun check() {
        try {
            mainIndicators.apply {
                clear()
                add(fetchMainIndicator("1"))
                add(fetchMainIndicator("2"))
            }
            lastOrderTime.apply {
                clear()
                add(fetchLastOrderRequestTime("1"))
                add(fetchLastOrderRequestTime("2"))
            }
            isAnyEnginesBehind = isAnyBehind()

            isAnyEnginesUnhealthy = !(mainIndicators.all { it })

            failedQueriesInRow = 0
        } catch (e: Exception) {
            failedQueriesInRow++
            logger.error("Unable to check matching-engine health", e)
        }
    }

    private fun isAnyBehind(): Boolean {
        val submitterTime = eventSubmitterInfo.lastSentOrderRequestTime ?: return false
        val submitterDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(submitterTime), ZoneId.systemDefault())

        if (lastOrderTime.isEmpty()) {
            logger.warn("No health indicator found in consul")
            return false
        }

        for (time in lastOrderTime) {
            if (time.plusMinutes(5).isBefore(submitterDateTime))
                return true
        }

        return false
    }

}