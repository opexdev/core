package co.nilin.opex.auth.data

import java.time.LocalDateTime

data class KycLevelUpdatedEvent(var userId: String, var kycLevel: KycLevel, var updateDate: LocalDateTime)

enum class KycLevel {
    LEVEL_1, LEVEL_2, LEVEL_3
}
