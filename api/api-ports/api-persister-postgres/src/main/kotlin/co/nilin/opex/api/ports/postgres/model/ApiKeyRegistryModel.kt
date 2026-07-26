package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("api_key_registry")
data class ApiKeyRegistryModel(
    @Id
    @Column("api_key_id")
    val apiKeyId: String,
    val label: String?,
    @Column("encrypted_secret")
    val encryptedSecret: String,
    val enabled: Boolean,
    @Column("allowed_ips")
    val allowedIps: String?,
    @Column("allowed_endpoints")
    val allowedEndpoints: String?,
    @Column("keycloak_user_id")
    val keycloakUserId: String?,
    @Column("keycloak_username")
    val keycloakUsername: String?,
    @Column("created_at")
    val createdAt: LocalDateTime,
    @Column("updated_at")
    val updatedAt: LocalDateTime
): Persistable<String> {
    override fun getId(): String = apiKeyId
    override fun isNew(): Boolean = createdAt == updatedAt
}