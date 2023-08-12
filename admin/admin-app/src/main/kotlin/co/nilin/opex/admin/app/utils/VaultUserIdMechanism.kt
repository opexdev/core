package co.nilin.opex.admin.app.utils

import org.springframework.vault.authentication.AppIdUserIdMechanism

class VaultUserIdMechanism() : AppIdUserIdMechanism {
    override fun createUserId(): String {
        return System.getenv("BACKEND_USER")
    }
}