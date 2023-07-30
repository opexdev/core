package co.nilin.opex.profile.core.data.profile

enum class KycLevelDetail(val kycLevel: KycLevel) {
    Registered(KycLevel.Level1),
    UploadDataForLevel2(KycLevel.Level1),
    SuccessfulManualReview(KycLevel.Level2),
    FailedManualReview(KycLevel.Level1);


    public val previousValidSteps: List<KycLevelDetail>?
        get() = when (this) {
            Registered -> null
            UploadDataForLevel2 -> arrayOf(Registered, FailedManualReview).asList()
            SuccessfulManualReview -> arrayOf(UploadDataForLevel2,FailedManualReview).asList()
            FailedManualReview -> arrayOf(UploadDataForLevel2,SuccessfulManualReview).asList()
        }

}
