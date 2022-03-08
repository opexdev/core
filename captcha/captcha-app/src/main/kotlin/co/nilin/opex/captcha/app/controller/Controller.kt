package co.nilin.opex.captcha.app.controller

import io.swagger.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class Controller() {
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
    suspend fun requestCaptchaSession(): String {
        return UUID.randomUUID().toString()
    }

    @ApiOperation(
        value = "Get captcha image",
        notes = "Get captcha image associated with provided uuid."
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
    @PatchMapping("/{uuid}", produces = [MediaType.IMAGE_JPEG_VALUE])
    suspend fun getCaptchaImage(@PathVariable uuid: String) {
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
    @DeleteMapping("/{uuid}")
    suspend fun verifyCaptcha(
        @PathVariable uuid: String,
        @RequestParam answer: String
    ) {
    }
}
