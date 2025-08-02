package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.SymbolMapModel
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SymbolMapRepository : CrudRepository<SymbolMapModel, String> {

    @Query("select * from symbol_maps where symbol = :symbol and alias_key = :aliasKey")
    fun findByAliasKeyAndSymbol(aliasKey: String, @Param("symbol") symbol: String): SymbolMapModel?

    @Query("select * from symbol_maps where alias_key = :aliasKey and alias = :alias")
    fun findByAliasKeyAndAlias(aliasKey: String, @Param("alias") alias: String): SymbolMapModel?

    fun findAllByAliasKey(aliasKey: String): List<SymbolMapModel>
}
