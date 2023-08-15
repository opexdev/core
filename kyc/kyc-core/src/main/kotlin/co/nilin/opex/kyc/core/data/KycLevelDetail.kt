package co.nilin.opex.kyc.core.data

enum class KycLevelDetail(val kycLevel: KycLevel) {
    Registered(KycLevel.Level1),
    UploadDataLevel2(KycLevel.Level1),
    AcceptedManualReview(KycLevel.Level2),
    RejectedManualReview(KycLevel.Level1),
    ManualUpdateLevel1(KycLevel.Level1),
    ManualUpdateLevel2(KycLevel.Level2);


    public val previousValidSteps: List<KycLevelDetail>?
        get() = when (this) {
            Registered -> null
            UploadDataLevel2 -> arrayOf(Registered, RejectedManualReview,ManualUpdateLevel1,ManualUpdateLevel2).asList()
            AcceptedManualReview -> arrayOf(UploadDataLevel2,RejectedManualReview,ManualUpdateLevel1,ManualUpdateLevel2).asList()
            RejectedManualReview -> arrayOf(UploadDataLevel2,AcceptedManualReview,ManualUpdateLevel1,ManualUpdateLevel2).asList()
            else -> {null}
        }

}
