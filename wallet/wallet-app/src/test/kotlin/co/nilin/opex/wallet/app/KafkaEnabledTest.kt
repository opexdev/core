package co.nilin.opex.wallet.app

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@Import(TestChannelBinderConfiguration::class)
abstract class KafkaEnabledTest {
    companion object {
        @Container
        val kafka = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.3")
        )

        @JvmStatic
        @DynamicPropertySource
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            kafka.start()
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }
    }
}
