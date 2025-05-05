package co.nilin.opex.auth.model

import java.time.Instant

data class ErrorResponse(
    val timestamp: Instant,    // Timestamp of the error
    val status: Int,           // HTTP status code
    val error: String,         // HTTP status reason phrase (e.g., "Bad Request")
    val message: String,       // Error message
    val path: String           // API path where the error occurred
)