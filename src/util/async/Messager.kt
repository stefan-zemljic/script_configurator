package util.async

import java.util.*

open class Messager<T : Any> {
    private val callbacks = HashSet<(T) -> Unit>()
    private val pending = LinkedList<T>() as Queue<T>
    private var sending = false

    open fun send(message: T) {
        pending.offer(message)
        if (!sending) {
            sending = true
            do {
                val current = pending.poll()
                callbacks.toList().forEach {
                    if (it in callbacks) it(current)
                }
            } while (pending.isNotEmpty())
            sending = false
        }
    }

    open fun listen(callback: (T) -> Unit) {
        callbacks.add(callback)
    }

    open fun unlisten(callback: (T) -> Unit) {
        callbacks.remove(callback)
    }
}