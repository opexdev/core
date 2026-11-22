package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.ports.postgres.dao.UserTransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.r2dbc.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Timestamp
import java.sql.Types

@Testcontainers(disabledWithoutDocker = true)
class UserTransactionOrderingTest {
    companion object {
        @Container
        @JvmField
        val database = PostgreSQLContainer<Nothing>("postgres:15-alpine")
    }

    private lateinit var jdbc: NamedParameterJdbcTemplate

    @BeforeEach
    fun setup() {
        jdbc = NamedParameterJdbcTemplate(DriverManagerDataSource(database.jdbcUrl, database.username, database.password))
        jdbc.jdbcTemplate.execute("drop table if exists user_transaction, wallet_owner")
        jdbc.jdbcTemplate.execute("create table wallet_owner(id integer primary key, uuid text, title text)")
        jdbc.jdbcTemplate.execute("""create table user_transaction(
            id serial primary key, uuid text unique, owner_id integer, tx_id bigint,
            currency text, balance numeric, balance_change numeric, category text,
            description text, date timestamp)""")
        val migration = javaClass.getResource("/db/migration/V18__user_transaction_correlation.sql")!!.readText()
        migration.split(';').filter { it.isNotBlank() }.forEach { jdbc.jdbcTemplate.execute(it) }
        jdbc.jdbcTemplate.execute("insert into wallet_owner values (1, 'customer', 'customer|Customer')")
        // Interleaved operations and identical timestamps reproduce unstable history ordering.
        insert("a-trade", 10, "TRADE", "ETH", "12:00:00", "a")
        insert("b-trade", 11, "TRADE", "ETH", "12:00:01", "b")
        insert("a-fee", 12, "FEE", "ETH", "12:00:02", "a")
        insert("b-fee", 13, "FEE", "USDT", "12:00:01", "b")
        insert("legacy-1", 14, "TRADE", "ETH", "12:00:03", null)
        insert("legacy-2", 15, "FEE", "ETH", "12:00:03", null)
    }

    private fun insert(id: String, tx: Long, category: String, currency: String, time: String, operation: String?) {
        jdbc.update("""insert into user_transaction
            (uuid, owner_id, tx_id, currency, balance, balance_change, category, date, operation_id, financial_action_id)
            values (:id, 1, :tx, :currency, 100, 1, :category, :date, :operation, :action)""",
            mapOf("id" to id, "tx" to tx, "currency" to currency, "category" to category,
                "date" to Timestamp.valueOf("2026-11-18 $time"), "operation" to operation, "action" to "$id-action"))
    }

    private fun history(asc: Boolean = false, currency: String? = null, limit: Int = 100, offset: Int = 0): List<Map<String, Any>> {
        val name = if (asc) "findUserTransactionHistoryAsc" else "findUserTransactionHistoryDesc"
        val query = UserTransactionRepository::class.java.methods.single { it.name == name }.getAnnotation(Query::class.java).value
        val parameters = MapSqlParameterSource()
            .addValue("userId", "customer", Types.VARCHAR)
            .addValue("currency", currency, Types.VARCHAR)
            .addValue("category", null, Types.VARCHAR)
            .addValue("startTime", null, Types.TIMESTAMP)
            .addValue("endTime", null, Types.TIMESTAMP)
            .addValue("limit", limit).addValue("offset", offset)
        return jdbc.queryForList(query, parameters)
    }

    @Test
    fun `groups operations before paging with trade before fee in either direction`() {
        val descending = listOf("legacy-2", "legacy-1", "b-trade", "b-fee", "a-trade", "a-fee")
        assertThat(history().map { it["id"] }).containsExactlyElementsOf(descending)
        assertThat(history(asc = true).map { it["id"] }).containsExactly(
            "a-trade", "a-fee", "b-trade", "b-fee", "legacy-1", "legacy-2")
        val pages = (0..2).flatMap { history(limit = 2, offset = it * 2).map { row -> row["id"] } }
        assertThat(pages).containsExactlyElementsOf(descending)
    }

    @Test
    fun `currency filtering preserves operation order and original posting data`() {
        val rows = history(currency = "ETH")
        assertThat(rows.map { it["id"] }).containsExactly("legacy-2", "legacy-1", "b-trade", "a-trade", "a-fee")
        val fee = rows.last()
        assertThat(fee["wallet_transaction_id"]).isEqualTo(12L)
        assertThat(fee["operation_id"]).isEqualTo("a")
        assertThat(fee["financial_action_id"]).isEqualTo("a-fee-action")
        assertThat(fee["date"]).isEqualTo(Timestamp.valueOf("2026-11-18 12:00:02"))
    }
}
