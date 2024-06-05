package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("rate")
data class RateModel(
        @Id var id: Long?,
        @Column("source_symbol") var sourceSymbol: Long,
        @Column("dest_symbol") var destinationSymbol: Long,
        var rate: BigDecimal,
        @Column("last_update_date") var lastUpdateDate: LocalDateTime= LocalDateTime.now(),
        @Column("create_date") var createDate: LocalDateTime)
