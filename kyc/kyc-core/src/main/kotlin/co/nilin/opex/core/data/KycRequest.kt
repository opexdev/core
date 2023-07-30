package co.nilin.opex.core.data

import co.nilin.opex.profile.core.data.profile.KycLevel
import co.nilin.opex.profile.core.data.profile.KycLevelDetail
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

data class KycRequest(
        var userId: String,
        var processId: String? = null,
        var issuer: String? = null,
        var step: KycStep? = null,
        var kycLevel: KycLevel? = null,
        var status: KycStatus? = null,
        var createDate: LocalDateTime? = LocalDateTime.now(),
        var description: String? = null,
        var frame1: MultipartFile?,
        var frame2: MultipartFile?,
        var frame3: MultipartFile?)
