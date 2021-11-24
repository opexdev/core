package co.nilin.opex.bcgateway.core.model

data class DepositResult(
    val latestBlock: Long,
    val deposits: List<Deposit>
)