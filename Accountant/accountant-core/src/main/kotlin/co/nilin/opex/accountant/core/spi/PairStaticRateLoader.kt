package co.nilin.opex.accountant.core.spi

interface PairStaticRateLoader {
    suspend fun calculateStaticRate(leftSide:String, rightSide: String): Double?
}