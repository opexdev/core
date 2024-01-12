package co.nilin.opex.common

import co.nilin.opex.utility.error.data.ErrorRep
import co.nilin.opex.utility.error.spi.ErrorConfig
import org.springframework.stereotype.Component

@Component
class CommonErrorConfig : ErrorConfig {

    override fun findErrorByCode(code: Int?): ErrorRep? {
        return OpexError.findByCode(code)
    }

    override fun internalServerError(): ErrorRep {
        return OpexError.InternalServerError
    }

    override fun invalidRequestParamError(): ErrorRep {
        return OpexError.InvalidRequestParam
    }
}