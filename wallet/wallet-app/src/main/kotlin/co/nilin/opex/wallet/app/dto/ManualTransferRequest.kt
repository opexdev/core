package co.nilin.opex.wallet.app.dto

import java.util.UUID

data class ManualTransferRequest(
        var description: String,
        var ref: String? = UUID.randomUUID().toString()
)
