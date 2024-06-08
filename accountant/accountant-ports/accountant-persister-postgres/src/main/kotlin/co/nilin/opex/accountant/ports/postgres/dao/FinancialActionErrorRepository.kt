package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.FinancialActionErrorModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FinancialActionErrorRepository : ReactiveCrudRepository<FinancialActionErrorModel, Long> {
}