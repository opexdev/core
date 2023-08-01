package co.nilin.opex.kyc.core.data


enum class KycStep() {
    UploadDataForLevel2(), ManualReview(), Register(), ManualUpdate()
}

enum class KycStatus {
    Successful,Failed,Reject,Accept
}