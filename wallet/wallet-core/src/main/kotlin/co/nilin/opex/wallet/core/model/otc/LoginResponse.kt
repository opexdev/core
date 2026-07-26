package co.nilin.opex.wallet.core.model.otc

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(var data: Token)

data class Token(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expireIn: Long
)
