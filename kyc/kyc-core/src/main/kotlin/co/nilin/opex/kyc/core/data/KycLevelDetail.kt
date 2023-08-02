package co.nilin.opex.kyc.core.data

enum class KycLevelDetail(val kycLevel: KycLevel) {
    Registered(KycLevel.Level1),
    UploadDataForLevel2(KycLevel.Level1),
    AcceptedManualReview(KycLevel.Level2),
    RejectedManualReview(KycLevel.Level1);


    public val previousValidSteps: List<KycLevelDetail>?
        get() = when (this) {
            Registered -> null
            UploadDataForLevel2 -> arrayOf(Registered, RejectedManualReview).asList()
            AcceptedManualReview -> arrayOf(UploadDataForLevel2,RejectedManualReview).asList()
            RejectedManualReview -> arrayOf(UploadDataForLevel2,AcceptedManualReview).asList()
        }

}
