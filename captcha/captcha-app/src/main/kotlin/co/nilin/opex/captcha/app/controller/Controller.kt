package co.nilin.opex.captcha.app.controller

import co.nilin.opex.captcha.app.api.CaptchaHandler
import co.nilin.opex.captcha.app.extension.sha256
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class Controller(private val captchaHandler: CaptchaHandler, private val store: MutableMap<String, String>) {
    @ApiOperation(
        value = "Get captcha image",
        notes = "Get captcha image associated with provided id."
    )
    @ApiResponses(
        ApiResponse(
            message = "OK",
            code = 200,
            response = String::class,
            examples = Example(
                ExampleProperty(
                    mediaType = MediaType.IMAGE_JPEG_VALUE,
                    value = "image.jpg"
                )
            )
        ),
        ApiResponse(
            message = "GONE",
            code = 410,
        )
    )
    @PostMapping(produces = [MediaType.IMAGE_JPEG_VALUE])
    suspend fun getCaptchaImage(): ResponseEntity<ByteArray> {
        val (text, image) = captchaHandler.generate()
        val id = UUID.randomUUID().toString().sha256().also { store[it] = text.sha256() }
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
        ),
        ApiResponse(
            message = "ANSWER_INVALID",
            code = 400,
        )
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    suspend fun verifyCaptcha(
        @PathVariable id: String,
        @RequestParam answer: String
    ) {
        store[id]?.let {
            if (captchaHandler.verify(answer, it)) store.remove(id) else throw OpexException(OpexError.BadRequest)
        } ?: throw OpexException(OpexError.NotFound)
    }
}
