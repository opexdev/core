package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("voucher_sale_data")
data class VoucherSaleDataModel(
    @Id val id: Long? = null,
    val voucher: Long,
    val nationalCode: String,
    val phoneNumber: String,
    val transactionNumber: String,
    val transactionAmount: BigDecimal,
    val saleDate: LocalDateTime,
    val sellerUuid: String,
)