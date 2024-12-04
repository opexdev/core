package co.nilin.opex.auth.core.data

import java.time.LocalDateTime

data class KycLevelUpdatedEvent(var userId: String, var kycLevel: KycLevel, var updateDate: LocalDateTime)
