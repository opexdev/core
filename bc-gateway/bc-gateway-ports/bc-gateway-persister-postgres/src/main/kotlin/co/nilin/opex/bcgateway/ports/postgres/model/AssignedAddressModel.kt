package co.nilin.opex.bcgateway.ports.postgres.model

import co.nilin.opex.bcgateway.core.model.AddressStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("assigned_addresses")
data class AssignedAddressModel(
    @Id val id: Long?,
    val uuid: String,
    val address: String,
    val memo: String?,
    @Column("addr_type_id") val addressTypeId: Long,
    @Column("exp_time") val expTime: LocalDateTime? = null,
    @Column("assigned_Date") val assignedDate: LocalDateTime? = null,
    @Column("revoked_Date") val revokedDate: LocalDateTime? = null,
    val status: AddressStatus? = null,


    )

