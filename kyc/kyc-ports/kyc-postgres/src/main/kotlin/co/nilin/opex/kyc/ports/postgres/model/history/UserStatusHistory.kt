package co.nilin.opex.kyc.ports.postgres.model.history

import co.nilin.opex.kyc.ports.postgres.model.base.UserStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("user_status_history")
data class UserStatusHistory(
        @Id
        var id: Long,
        var changeIssuer: String?,
        var changeRequestDate: LocalDateTime?,
        var changeRequestType: String?
) : UserStatus()