package co.nilin.opex.profile.ports.postgres.model.history

import co.nilin.opex.profile.ports.postgres.model.base.Limitations
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("revoke_permission_history")
data class LimitationHistory(
        @Id
        var id: Long,
        var originalDataId: Long?,
        var issuer: String?,
        var changeRequestDate: LocalDateTime?,
        var changeRequestType: String?
) : Limitations()