package co.nilin.opex.captcha.app.controller

import co.nilin.opex.captcha.app.api.CaptchaHandler
import co.nilin.opex.captcha.app.extension.sha256
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@RestController
class Controller(
    private val captchaHandler: CaptchaHandler,
    private val store: ConcurrentHashMap<String, Long>,
    @Value("\${app.captcha-window-seconds}") private val captchaWindowSeconds: Long
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
        val (text, image) = captchaHandler.generate()
        val id = UUID.randomUUID().toString().sha256().also { store["$it:$text".sha256()] = System.currentTimeMillis() }
        return ResponseEntity(image, HttpHeaders().apply { set("captcha-session-key", id) }, HttpStatus.OK)
    }

    @ApiOperation(value = "Verify captcha", notes = "Verify captcha.")
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
    suspend fun verifyCaptcha(
        @RequestParam id: String,
        @RequestParam answer: String
    ) {
        val ms = System.currentTimeMillis()
        store.filterValues { it <= ms - captchaWindowSeconds * 1000 }.forEach { store.remove(it.key) }
        if (store.contains("$id:$answer".sha256())) store.remove(id) else throw OpexException(OpexError.NotFound)
    }
}
