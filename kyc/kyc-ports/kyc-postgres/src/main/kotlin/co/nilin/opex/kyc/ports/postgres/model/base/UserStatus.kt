package co.nilin.opex.kyc.ports.postgres.model.base

import co.nilin.opex.kyc.core.data.KycLevelDetail
import java.time.LocalDateTime

open class UserStatus {
    lateinit var userId: String;
    lateinit var kycLevel: KycLevelDetail
    var referenceId: String? = null
    var description: String? = null
    var detail: String? = null
    var lastUpdateDate: LocalDateTime? = LocalDateTime.now()


}
