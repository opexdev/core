package co.nilin.opex.core.data

import co.nilin.opex.profile.core.data.profile.KycLevel
import co.nilin.opex.profile.core.data.profile.KycLevelDetail
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

 open class KycRequest {
     lateinit var userId: String
      var processId: String? = null
     var issuer: String? = null
     var step: KycStep? = null
     var createDate: LocalDateTime? = LocalDateTime.now()
     var description: String? = null
 }
