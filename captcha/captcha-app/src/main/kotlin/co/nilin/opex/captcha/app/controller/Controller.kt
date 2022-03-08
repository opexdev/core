package co.nilin.opex.captcha.app.controller

import co.nilin.opex.captcha.app.api.CaptchaHandler
import co.nilin.opex.captcha.app.extension.sha256
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import io.swagger.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class Controller(private val captchaHandler: CaptchaHandler, private val store: MutableMap<String, String>) {
    @ApiOperation(
        value = "Request new captcha session",
        notes = "Request new captcha session."
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        response = String::class,
        examples = Example(
            ExampleProperty(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                value = "ef83c977-abae-452a-81c8-5455f4e3a9fa"
            )
        )
    )
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun createSession() = UUID.randomUUID().toString().sha256().also { store[it] = "" }

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
    @PutMapping("/{id}", produces = [MediaType.IMAGE_JPEG_VALUE])
    suspend fun getCaptchaImage(@PathVariable id: String): ByteArray {
        store[id] ?: throw OpexException(OpexError.Error, status = HttpStatus.GONE)
        val (text, image) = captchaHandler.generate()
        store[id] = text.sha256()
        return image
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
