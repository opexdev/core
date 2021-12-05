package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("chain_sync_retry")
class ChainSyncRetryModel(
    val chain: String,
    val block: Long,
    var retries: Int = 1,
    var synced: Boolean = false,
    @Column("give_up")
    var giveUp: Boolean = false,
    var error: String? = null,
    @Id
    var id: Long? = null
)