package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("rate")
data class RateModel(
    @Id var id: Long?,
    var sourceSymbol: String,
    @Column("dest_symbol")
    var destinationSymbol: String,
    var rate: BigDecimal,
    var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
    var createDate: LocalDateTime
)
