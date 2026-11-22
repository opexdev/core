package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.ports.postgres.dao.FundingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.r2dbc.repository.Query
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Timestamp
import java.sql.DriverManager

@Testcontainers(disabledWithoutDocker = true)
class FundingReportQueryTest {
    companion object {
        @Container @JvmField
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")
    }

    @Test
    fun `selects the latest state as of period end and never counts a cancellation or rejection`() {
        val connection = DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
        connection.createStatement().use { it.execute("""CREATE TABLE opex_funding_operations (
            event_id TEXT, operation_id TEXT, type TEXT, status TEXT, occurred_at TIMESTAMP)""") }
        fun put(id: String, status: String, time: String, type: String = "WITHDRAW") {
            connection.prepareStatement("INSERT INTO opex_funding_operations VALUES (?,?,?,?,?)").use {
                it.setString(1, "$id-$status")
                it.setString(2, id)
                it.setString(3, type)
                it.setString(4, status)
                it.setTimestamp(5, Timestamp.valueOf("2026-11-18 $time"))
                it.executeUpdate()
            }
        }
        put("done", "ACCEPTED", "10:00:00")
        put("done", "DONE", "11:00:00")
        put("pending", "ACCEPTED", "10:00:00")
        put("canceled", "ACCEPTED", "10:00:00")
        put("canceled", "CANCELED", "11:00:00")
        put("rejected", "ACCEPTED", "10:00:00")
        put("rejected", "REJECTED", "11:00:00")
        put("deposit", "DONE", "10:00:00", "DEPOSIT")
        put("invalid", "INVALID", "10:00:00", "DEPOSIT")
        val query = FundingRepository::class.java.methods.single { it.name == "findReportable" }
            .getAnnotation(Query::class.java).value
        fun rows(end: String): List<Pair<String, String>> {
            return connection.prepareStatement(query.replace(":to", "?").replace(":from", "?").replace(":limit", "?")).use {
                it.setTimestamp(1, Timestamp.valueOf("2026-11-18 $end"))
                it.setTimestamp(2, Timestamp.valueOf("2026-11-18 00:00:00"))
                it.setInt(3, 100)
                it.executeQuery().use { result ->
                    buildList { while (result.next()) add(result.getString("operation_id") to result.getString("status")) }
                }
            }
        }
        val after = rows("12:00:00")
        assertThat(after.map { it.first }).containsExactlyInAnyOrder("done", "pending", "deposit")
        assertThat(after.single { it.first == "done" }.second).isEqualTo("DONE")
        assertThat(rows("10:30:00").single { it.first == "done" }.second).isEqualTo("ACCEPTED")
        connection.close()
    }
}
