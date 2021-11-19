package co.nilin.opex.port.websocket.service.stream

abstract class StreamHandler<T>(protected val base: String) {

    protected val map = hashMapOf<String, PathPool<T>>()

    fun addSubscription(path: String, pathType: T, sessionId: String, data: Array<Any>) {
        if (!isPathSubscribable(path))
            return

        if (map[path] == null) {
            map[path] = PathPool(path, pathType, data).apply { addSubscription(sessionId) }
        } else {
            map[path]?.addSubscription(sessionId)
        }
    }

    fun removeSubscription(path: String, sessionId: String) {
        map[path]?.removeSubscription(sessionId)
    }

    fun countSubscribers(): Int {
        var sum = 0
        map.entries.forEach { sum += it.value.numberOfSubscribers() }
        return sum
    }

    fun getPaths() = map.entries.map { it.key }

    abstract fun isPathSubscribable(path: String): Boolean

}