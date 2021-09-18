package co.nilin.opex.utility.error.data

import co.nilin.opex.utility.error.spi.ExceptionResponse
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class DefaultExceptionResponse(
    val error: String?,
    val code: Int,
    val message: String?,
    status: HttpStatus,
    val data: Any? = null,
    @JsonIgnore
    val crimeScene: Class<*>?,
    val timestamp: Date = Date()
) : ExceptionResponse(status)