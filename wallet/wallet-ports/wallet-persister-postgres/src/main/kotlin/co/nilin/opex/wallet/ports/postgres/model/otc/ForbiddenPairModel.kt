package co.nilin.opex.wallet.ports.postgres.model.otc

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("forbidden_rate")
data class ForbiddenPairModel (
        @Id
    var id: Long?,
        @Column("source_symbol") var sourceSymbol: String,
        @Column("destination_symbol") var destinationSymbol: String,
        @Column("last_update_date") var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
        @Column("create_date") var createDate: LocalDateTime
)