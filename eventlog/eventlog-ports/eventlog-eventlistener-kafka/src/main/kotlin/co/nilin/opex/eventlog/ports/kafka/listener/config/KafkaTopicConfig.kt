package co.nilin.opex.eventlog.ports.kafka.listener.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext

@Configuration
class KafkaTopicConfig {

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        with(applicationContext) {
            registerBean("topic_events.DLT", NewTopic::class.java, "events.DLT", 10, 1)
            registerBean("topic_orders.DLT", NewTopic::class.java, "orders.DLT", 10, 1)
            registerBean("topic_trades.DLT", NewTopic::class.java, "trades.DLT", 10, 1)
            registerBean("topic_tempevents.DLT", NewTopic::class.java, "tempevents.DLT", 10, 1)
            registerBean("topic_richTrade.DLT", NewTopic::class.java, "richTrade.DLT", 10, 1)
            registerBean("topic_richOrder.DLT", NewTopic::class.java, "richOrder.DLT", 10, 1)
            registerBean("topic_admin_event.DLT", NewTopic::class.java, "admin_event.DLT", 10, 1)
            registerBean("topic_auth_user_created.DLT", NewTopic::class.java, "auth_user_created.DLT", 10, 1)
        }
    }

}