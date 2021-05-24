package co.nilin.mixchange.accountant.core.spi

interface PairStaticRateLoader {
    suspend fun calculateStaticRate(leftSide:String, rightSide: String): Double?
}