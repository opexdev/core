package co.nilin.opex.core.data

import co.nilin.opex.profile.core.data.profile.KycLevel

enum class KycStep() {
    UploadDataForLevel2(), ManualReview(), Register(), ManualUpdate()
}

enum class KycStatus {
    Successful,Failed,Reject,Accept
}