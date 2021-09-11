package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Endpoint(val url: String)
data class Chain(val name: String, val addressTypes: List<AddressType>, val endpoints: List<Endpoint>)
data class ChainSyncSchedule(val chainName: String, val retryTime: LocalDateTime, val delay: Long)
data class ChainSyncRecord(
    val chainName: String,
    val time: LocalDateTime,
    val endpoint: Endpoint,
    val latestBlock: Long?,
    val success: Boolean,
    val error: String?,
    val records: List<Deposit>
)

