package co.nilin.opex.accountant.app

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(TestChannelBinderConfiguration::class)
class AccountantAppTest {
    @Test
    fun contextLoad() {

    }
}