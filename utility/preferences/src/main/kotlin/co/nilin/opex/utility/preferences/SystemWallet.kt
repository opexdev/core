package co.nilin.opex.utility.preferences

import java.util.UUID

data class SystemWallet(var uuid: UUID, var title: String, var level: String, var schedule: WalletSyncSchedule)
