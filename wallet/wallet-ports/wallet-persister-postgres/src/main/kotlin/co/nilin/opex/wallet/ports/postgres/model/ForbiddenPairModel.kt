package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("forbidden_pair")
data class ForbiddenPairModel(
    @Id var id: Long?,
    var sourceSymbol: String,
    @Column("dest_symbol")
    var destinationSymbol: String,
    var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
    var createDate: LocalDateTime
)