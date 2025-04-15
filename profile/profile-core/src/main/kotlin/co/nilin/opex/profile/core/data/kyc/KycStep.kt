package co.nilin.opex.profile.core.data.kyc


enum class KycStep() {
    UploadDataForLevel3(), ManualReview(), Register(), ManualUpdate() , ProfileCompleted()
}

enum class KycStatus {
    Successful, Failed, Rejected, Accepted
}

enum class KycMethod{
    METHOD_1 , METHOD_2
}