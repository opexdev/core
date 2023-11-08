package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResultDetailed

interface TransferManager {

    suspend fun transfer(transferCommand: TransferCommand): TransferResultDetailed
}