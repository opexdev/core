package co.nilin.opex.captcha.app.api

interface SessionStore {
    fun put(proof: String)
    fun remove(proof: String): Boolean
    fun contains(proof: String): Boolean
}
