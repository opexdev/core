import co.nilin.opex.kyc.core.data.KycLevel
import co.nilin.opex.kyc.core.data.ManualUpdateRequest
import org.junit.jupiter.api.Test


class TestDataClass {


    @Test
    fun testManualUpdate(manualUpdateRequest: ManualUpdateRequest) {

    }

    @Test
    fun testManualUpdateRequest() {
      val request=  ManualUpdateRequest(kycLevel = KycLevel.Level1)
        request.issuer=""
    }

}