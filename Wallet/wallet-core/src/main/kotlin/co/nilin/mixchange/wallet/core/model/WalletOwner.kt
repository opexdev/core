package co.nilin.mixchange.wallet.core.model

interface WalletOwner {
   fun id(): Long?
   fun uuid(): String
   fun title(): String
   fun level(): String
}