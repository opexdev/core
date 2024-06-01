package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "fi_action_retry")
data class FinancialActionRetryModel(
    val faId: Long,
    var nextRunTime: LocalDateTime,
    var retries: Int = 0,
    var isResolved: Boolean = false,
    var hasGivenUp: Boolean = false,
    @Id var id: Long? = null
)