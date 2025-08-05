package co.nilin.opex.profile.core.data.kyc

enum class  KycLevelDetail(val kycLevel: KycLevel) {
    Registered(KycLevel.LEVEL_1),
    ProfileCompleted(KycLevel.LEVEL_2),
    UploadDataLevel3(KycLevel.LEVEL_2),
    AcceptedManualReview(KycLevel.LEVEL_3),
    RejectedManualReview(KycLevel.LEVEL_2),
    ManualUpdateLevel1(KycLevel.LEVEL_1),
    ManualUpdateLevel2(KycLevel.LEVEL_2),
    ManualUpdateLevel3(KycLevel.LEVEL_3);


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
