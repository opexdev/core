package co.nilin.opex.profile.ports.postgres.model.history

import co.nilin.opex.profile.ports.postgres.model.base.Limitation
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("limitation_history")
data class LimitationHistory(
        @Id
        var id: Long,
        var issuer: String?,
        var changeRequestDate: LocalDateTime?,
        var changeRequestType: String?
) : Limitation()