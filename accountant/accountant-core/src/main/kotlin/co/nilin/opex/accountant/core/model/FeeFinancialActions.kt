package co.nilin.opex.accountant.core.model

data class FeeFinancialActions(
    val makerFeeAction: FinancialAction,
    val takerFeeAction: FinancialAction
)