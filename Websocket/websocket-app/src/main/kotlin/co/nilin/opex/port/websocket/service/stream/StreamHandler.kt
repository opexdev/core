package co.nilin.opex.port.websocket.service.stream

abstract class StreamHandler<T> {

    protected val map = hashMapOf<String, PathPool<T>>()

    fun addSubscription(path: String, pathType: T, sessionId: String) {
        if (!isPathSubscribable(path))
            return

        if (map[path] == null) {
            map[path] = PathPool(path, pathType).apply { addSub(sessionId) }
        } else {
            map[path]?.addSub(sessionId)
        }
    }

    fun removeSubscription(path: String, sessionId: String) {
        map[path]?.removeSub(sessionId)
    }

    fun countSubscribers(): Int {
        var sum = 0
        map.entries.forEach { sum += it.value.numberOfSubscribers() }
        return sum
    }

    abstract fun isPathSubscribable(path: String): Boolean

}