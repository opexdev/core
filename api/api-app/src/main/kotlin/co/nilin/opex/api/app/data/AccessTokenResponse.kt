package co.nilin.opex.api.app.data

data class AccessTokenResponse(
    val access_token: String,
    val refresh_token: String,
)