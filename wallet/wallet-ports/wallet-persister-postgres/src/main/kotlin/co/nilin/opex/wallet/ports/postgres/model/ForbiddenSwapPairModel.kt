package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("forbidden_swap_pair")
data class ForbiddenSwapPairModel(
    @Id var id: Long?,
    var sourceSymbol: String,
    @Column("dest_symbol")
    var destinationSymbol: String,
    var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
    var createDate: LocalDateTime
)