package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.impl.FinancialActionLoaderImpl
import io.mockk.mockk

class FALoaderImplTest {

    private val financialActionRepository = mockk<FinancialActionRepository>()
    private val faLoader = FinancialActionLoaderImpl(financialActionRepository)

}