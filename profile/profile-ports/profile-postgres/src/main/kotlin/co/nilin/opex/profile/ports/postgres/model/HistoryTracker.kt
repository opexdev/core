package co.nilin.opex.profile.ports.postgres.model

import java.util.*

data class HistoryTracker(
        var originalDataId: Long?,
        var issuer: String?,
        var changeRequestDate: Date?,
        var changeRequestType: String?
)