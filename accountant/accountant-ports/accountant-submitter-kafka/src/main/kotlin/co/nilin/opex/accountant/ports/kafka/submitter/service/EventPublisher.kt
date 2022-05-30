package co.nilin.opex.accountant.ports.kafka.submitter.service

interface EventPublisher {
    fun topic(): String
}