package co.nilin.opex.kyc.core.data

import java.time.LocalDateTime

data class UserLevelHistory(var userId: String,
                            var kycLevel: KycLevelDetail,
                            var referenceId: String?,
                            var description: String?,
                            var detail: String?,
                            var lastUpdateDate: LocalDateTime?,
                            var changeIssuer: String?,
                            var changeRequestDate: LocalDateTime?,
                            var changeRequestType: String?
)
