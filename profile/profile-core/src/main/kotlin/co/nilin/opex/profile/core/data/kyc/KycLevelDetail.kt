package co.nilin.opex.profile.core.data.kyc

enum class  KycLevelDetail(val kycLevel: KycLevel) {
    Registered(KycLevel.Level1),
    ProfileCompleted(KycLevel.Level2),
    UploadDataLevel3(KycLevel.Level2),
    AcceptedManualReview(KycLevel.Level3),
    RejectedManualReview(KycLevel.Level2),
    ManualUpdateLevel1(KycLevel.Level1),
    ManualUpdateLevel2(KycLevel.Level2),
    ManualUpdateLevel3(KycLevel.Level3);


    public val previousValidSteps: List<KycLevelDetail>?
        get() = when (this) {
            Registered -> null
            ProfileCompleted -> arrayOf(Registered).asList()
            UploadDataLevel3 -> arrayOf(Registered, RejectedManualReview, ManualUpdateLevel1, ManualUpdateLevel3,ProfileCompleted).asList()
            AcceptedManualReview -> arrayOf(UploadDataLevel3, RejectedManualReview, ManualUpdateLevel1,ManualUpdateLevel2, ManualUpdateLevel3).asList()
            RejectedManualReview -> arrayOf(UploadDataLevel3, AcceptedManualReview, ManualUpdateLevel1,ManualUpdateLevel2, ManualUpdateLevel3).asList()
            else -> {
                null
            }
        }

}
