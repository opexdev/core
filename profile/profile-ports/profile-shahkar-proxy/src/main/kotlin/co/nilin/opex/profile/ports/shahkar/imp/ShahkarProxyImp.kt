package co.nilin.opex.profile.ports.shahkar.imp

import co.nilin.opex.profile.core.spi.ShahkarInquiry
import org.springframework.stereotype.Component

@Component
class ShahkarProxyImp : ShahkarInquiry {
    override suspend fun getInquiryResult(identifier: String, mobile: String): Boolean {
        //TODO implement
        return true
    }
}