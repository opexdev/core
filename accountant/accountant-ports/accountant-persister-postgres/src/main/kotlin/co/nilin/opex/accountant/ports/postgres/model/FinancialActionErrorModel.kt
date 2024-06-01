package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "fi_action_error")
data class FinancialActionErrorModel(
    val faId: Long,
    val error: String,
    val message: String?,
    val body: String?,
    val retryId: Long? = null,
    val date: LocalDateTime = LocalDateTime.now(),
    @Id var id: Long? = null
)