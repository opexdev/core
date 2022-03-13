package co.nilin.opex.auth.gateway.utils

import co.nilin.opex.utility.error.DefaultErrorTranslator
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import javax.ws.rs.core.Response

object ErrorHandler {

    private val translator = DefaultErrorTranslator()

    fun response(status: Response.Status, ex: OpexException): Response {
        return Response.status(status).entity(translator.translate(ex)).build()
    }

    fun forbidden() = response(Response.Status.FORBIDDEN, OpexException(OpexError.Forbidden))

    fun userNotFound() = ErrorHandler.response(Response.Status.NOT_FOUND, OpexException(OpexError.UserNotFound))

}