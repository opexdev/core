package co.nilin.opex.common.utils



fun justTry(action: () -> Unit) {
    try {
        action()
    } catch (_: Exception) {
    }
}

