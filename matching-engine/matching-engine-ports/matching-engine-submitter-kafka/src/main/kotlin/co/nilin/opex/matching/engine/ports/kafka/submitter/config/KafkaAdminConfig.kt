package co.nilin.opex.matching.engine.ports.kafka.submitter.config

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.DescribeClusterOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class KafkaAdminConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    @Bean
    fun adminClient(admin: KafkaAdmin): AdminClient {
        return AdminClient.create(admin.configurationProperties)
    }

    @Bean
    fun kafkaHealthIndicator(adminClient: AdminClient): HealthIndicator {
        val options = DescribeClusterOptions().timeoutMs(1000)
        return object : AbstractHealthIndicator() {

            override fun doHealthCheck(builder: Health.Builder) {
                try {
                    adminClient.describeCluster(options)
                    builder.up().build()
                }catch (e:Exception){
                    builder.down(e).build()
                }
            }
        }
    }

}