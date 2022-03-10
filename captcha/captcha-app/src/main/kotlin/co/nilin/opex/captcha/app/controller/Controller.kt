package co.nilin.opex.captcha.app.controller

import co.nilin.opex.captcha.app.api.CaptchaHandler
import co.nilin.opex.captcha.app.extension.sha256
import co.nilin.opex.captcha.app.api.SessionStore
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class Controller(
    private val captchaHandler: CaptchaHandler,
    private val sessionStore: SessionStore
) {
    @ApiOperation(
        value = "Get captcha image",
        notes = "Get captcha image associated with provided id."
    )
    @ApiResponses(
        ApiResponse(message = "OK", code = 200),
        ApiResponse(message = "GONE", code = 410)
    )
    @PostMapping("/session", produces = [MediaType.IMAGE_JPEG_VALUE])
    suspend fun getCaptchaImage(): ResponseEntity<ByteArray> {
        fun idGen(id: String = UUID.randomUUID().toString().sha256()): String =
            if (sessionStore.contains(id)) idGen() else id
        val (answer, image) = captchaHandler.generate()
        val id = idGen()
        sessionStore.put("$id:$answer".sha256())
        return ResponseEntity(image, HttpHeaders().apply { set("captcha-session-key", id) }, HttpStatus.OK)
    }

    @ApiOperation(
        value = "Verify captcha",
        notes = "Verify captcha. proof is a string in form of \"{{captcha-session-key}}:{{answer}}\""
    )
    @ApiResponses(
        ApiResponse(
            message = "OK",
            code = 204,
        ),
        ApiResponse(
            message = "SESSION_NOT_FOUND",
            code = 404,
        )
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping
    suspend fun verifyCaptcha(@RequestParam proof: String) {
        if (!sessionStore.remove(proof.sha256())) throw OpexException(OpexError.NotFound)
    }
}