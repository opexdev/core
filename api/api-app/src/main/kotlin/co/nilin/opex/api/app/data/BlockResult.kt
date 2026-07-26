package co.nilin.opex.api.app.data

data class BlockResult(
    val blocked: Boolean,
    val retryAfterSeconds: Int = 0
)