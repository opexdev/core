package co.nilin.opex.kyc.core.data.event

import co.nilin.opex.kyc.core.data.KycLevel
import java.time.LocalDateTime

data class KycLevelUpdatedEvent(var userId:String, var kycLevel: KycLevel, var updateDate:LocalDateTime)
