package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("detail_assets_snapshot")
data class DetailAssetsSnapshotModel(
    @Id
    val id: Long? = null,
    val uuid: String,
    val currency: String,
    val volume: BigDecimal,
    val totalAmount: BigDecimal,
    val quoteCurrency: String,
    val snapshotDate: LocalDateTime,
)