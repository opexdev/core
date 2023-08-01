package co.nilin.opex.kyc.core.data.event

import co.nilin.opex.profile.core.data.profile.KycLevel
import java.time.LocalDateTime

data class KycLevelUpdatedEvent(var userId:String, var kycLevel:KycLevel, var updateDate:LocalDateTime)
