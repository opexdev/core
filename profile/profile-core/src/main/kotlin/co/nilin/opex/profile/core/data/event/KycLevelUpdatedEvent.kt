package co.nilin.opex.profile.core.data.event

import co.nilin.opex.profile.core.data.kyc.KycLevel
import java.time.LocalDateTime

data class KycLevelUpdatedEvent(var userId: String, var kycLevel: KycLevel, var updateDate: LocalDateTime)
