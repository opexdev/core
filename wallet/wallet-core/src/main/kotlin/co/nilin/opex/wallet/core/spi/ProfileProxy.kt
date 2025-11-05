package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.profile.Profile


interface ProfileProxy {
    suspend fun getProfile(token: String): Profile
}