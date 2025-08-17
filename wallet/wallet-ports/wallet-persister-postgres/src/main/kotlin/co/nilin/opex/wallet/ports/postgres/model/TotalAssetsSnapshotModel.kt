package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("total_assets_snapshot")
data class TotalAssetsSnapshotModel(
    @Id
    val id: Long? = null,
    val owner: Long,
    @Column("total_irt")
    val totalIRT: BigDecimal,
    @Column("total_usdt")
    val totalUSDT: BigDecimal,
    val snapshotDate: LocalDateTime,
)