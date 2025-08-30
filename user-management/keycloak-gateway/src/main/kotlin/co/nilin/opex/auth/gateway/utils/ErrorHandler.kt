package co.nilin.opex.auth.gateway.utils

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.translation.CustomErrorTranslator
import co.nilin.opex.common.utils.userLanguageResolver
import co.nilin.opex.utility.error.data.OpexException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

object ErrorHandler {


    private val translator = CustomErrorTranslator()

    fun response(
        status: Response.Status,
        error: OpexError,
        message: String? = null
    ): Response {

        return Response.status(status)
            .entity(translator.translate(OpexException(error, message), userLanguageResolver()))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build()

    }

    fun forbidden(message: String? = null) = response(Response.Status.FORBIDDEN, OpexError.Forbidden, message)

    fun badRequest(message: String? = null) = response(Response.Status.BAD_REQUEST, OpexError.BadRequest, message)

    fun notFound(message: String? = null) = response(Response.Status.NOT_FOUND, OpexError.NotFound, message)

    fun userNotFound(message: String? = null) = response(Response.Status.NOT_FOUND, OpexError.UserNotFound, message)


}