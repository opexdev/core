package co.nilin.opex.wallet.app.dto

import java.util.UUID

data class ManualTransferRequest(
        var ref: String,
        var description: String? = null,
        )
