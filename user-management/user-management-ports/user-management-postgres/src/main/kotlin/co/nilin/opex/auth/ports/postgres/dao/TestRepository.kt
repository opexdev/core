package co.nilin.opex.auth.ports.postgres.dao

import co.nilin.opex.auth.ports.postgres.model.Test
import kotlinx.coroutines.flow.Flow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestRepository :JpaRepository<Test,Long> {
    fun findAllBy(): Flow<Test>?
}