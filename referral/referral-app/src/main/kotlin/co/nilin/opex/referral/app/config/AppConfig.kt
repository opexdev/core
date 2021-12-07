package co.nilin.opex.referral.app.config

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.referral.core.spi.CommissionRewardPersister
import co.nilin.opex.referral.ports.kafka.listener.consumer.TradeKafkaListener
import co.nilin.opex.referral.ports.kafka.listener.spi.RichTradeListener
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun apiListener(
        commissionRewardPersister: CommissionRewardPersister
    ): ReferralListenerImpl {
        return ReferralListenerImpl(commissionRewardPersister)
    }

    @Autowired
    fun configureListeners(
        tradeKafkaListener: TradeKafkaListener,
        appListener: ReferralListenerImpl
    ) {
        tradeKafkaListener.addTradeListener(appListener)
    }

    class ReferralListenerImpl(
        private val commissionRewardPersister: CommissionRewardPersister
    ) : RichTradeListener {

        override fun id(): String {
            return "ReferralListener"
        }

        override fun onTrade(
            trade: RichTrade,
            partition: Int,
            offset: Long,
            timestamp: Long
        ) {
            println("RichTrade received")
            runBlocking(AppDispatchers.kafkaExecutor) {
                commissionRewardPersister.save(trade)
            }
        }
    }
}