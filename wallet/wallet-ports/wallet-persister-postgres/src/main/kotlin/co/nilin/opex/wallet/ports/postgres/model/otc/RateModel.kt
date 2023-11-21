package co.nilin.opex.wallet.ports.postgres.model.otc

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("rate")
data class RateModel(
        @Id var id: Long?,
        @Column("source_symbol") var sourceSymbol: String,
        @Column("destination_symbol") var destinationSymbol: String,
        var rate: BigDecimal?,
        @Column("last_update_date") var lastUpdateDate: LocalDateTime,
        @Column("is_forbidden") var isForbidden: Boolean? = false
)
