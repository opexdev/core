package co.nilin.opex.market.ports.postgres.util

import co.nilin.opex.market.core.inout.Transaction
import co.nilin.opex.market.core.inout.TransactionDto
import java.time.ZoneId
import java.util.*


fun Transaction.toDto(): TransactionDto {
    return TransactionDto(
        createDate = Date.from(createDate.atZone(ZoneId.systemDefault()).toInstant()),
        volume,
        transactionPrice,
        matchedPrice,
        side,
        symbol,
        fee,
        user
    )


}
