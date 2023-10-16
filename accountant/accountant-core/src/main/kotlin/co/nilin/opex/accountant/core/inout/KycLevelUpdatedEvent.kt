package co.nilin.opex.accountant.core.inout


import co.nilin.opex.accountant.core.model.KycLevel
import java.time.LocalDateTime

data class KycLevelUpdatedEvent(var userId: String, var kycLevel: KycLevel, var updateDate: LocalDateTime)
