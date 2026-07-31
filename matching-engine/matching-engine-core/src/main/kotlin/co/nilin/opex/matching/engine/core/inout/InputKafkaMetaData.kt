package co.nilin.opex.matching.engine.core.inout

data class InputKafkaMetadata(
    val topic: String,
    val partition: Int,
    val offset: Long,
    val consumerGroupId: String
) {
    val nextOffset: Long
        get() = offset + 1
}