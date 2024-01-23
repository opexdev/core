package co.nilin.opex.wallet.core.model

object TokenHolder {
    private val threadLocalToken = ThreadLocal<String>()

    fun setToken(token: String) {
        threadLocalToken.set(token)
    }

    fun getToken(): String? {
        return threadLocalToken.get()
    }

    fun clearToken() {
        threadLocalToken.remove()
    }
}