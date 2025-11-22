package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.Profile

interface ProfileProxy {

    suspend fun getProfile(uuid: String, token: String): Profile?
}