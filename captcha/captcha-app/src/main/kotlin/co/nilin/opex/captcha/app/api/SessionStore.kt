package co.nilin.opex.captcha.app.api

interface SessionStore {
    fun put(proof: String): Long
    fun remove(proof: String): Boolean
    fun verify(proof: String): Boolean
}
