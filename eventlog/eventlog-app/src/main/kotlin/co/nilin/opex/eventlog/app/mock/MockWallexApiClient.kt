package co.nilin.opex.eventlog.app.mock

import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Component
class MockWallexApiClient {

    fun fetchWalletMovements(from: LocalDateTime, to: LocalDateTime, limit: Int): List<WallexWalletMovement> {
        val totalMinutes = kotlin.math.max(1L, java.time.Duration.between(from, to).toMinutes())
        val steps = limit.coerceIn(1, 300)
        return (0 until steps).map { index ->
            val minuteOffset = (totalMinutes * (index + 1) / (steps + 1)).coerceAtLeast(1)
            val occurredAt = from.plusMinutes(minuteOffset)
            val isDeposit = index % 2 == 0
            val userId = UUID.nameUUIDFromBytes("wallex-user-$index".toByteArray()).toString()
            WallexWalletMovement(
                txId = UUID.nameUUIDFromBytes("wallex-tx-$index-$occurredAt".toByteArray()).toString(),
                userId = userId,
                side = if (isDeposit) "DEPOSIT" else "WITHDRAW",
                asset = if (index % 3 == 0) "IRT" else "USDT",
                amount = BigDecimal.valueOf(500_000L + (index * 45_000L)),
                fee = BigDecimal.valueOf(2_500L + (index * 100L)),
                referenceId = "wallex-${occurredAt.toLocalDate()}-$index",
                occurredAt = occurredAt
            )
        }
    }
}

data class WallexWalletMovement(
    val txId: String,
    val userId: String,
    val side: String,
    val asset: String,
    val amount: BigDecimal,
    val fee: BigDecimal,
    val referenceId: String,
    val occurredAt: LocalDateTime
)
