package util.async

import javafx.beans.property.Property

class Box<T : Any>(value: T) : Messager<T>() {
    var value: T = value
        private set

    fun setValue(newValue: T) {
        if (value === newValue) return
        value = newValue
        send(value)
    }

    override fun listen(callback: (T) -> Unit) {
        super.listen(callback)
        callback(value)
    }

    fun <S : Any> with(other: Box<S>): Box<Pair<T, S>> {
        val combine = { Pair(value, other.value) }
        val box = Box(combine())
        listen { box.setValue(combine()) }
        other.listen { box.setValue(combine()) }
        box.listen { setValue(it.first); other.setValue(it.second) }
        return box
    }

    fun <R : Any> map(
        result: Box<R>? = null,
        reducer: (T) -> R,
    ) = map(reducer, null, result)

    fun <R : Any> map(
        reducer: (T) -> R,
        reverse: ((R) -> T)? = null,
        result: Box<R>? = null,
    ): Box<R> {
        val box = result ?: Box(reducer(value))
        listen { box.setValue(reducer(it)) }
        if (reverse != null)
            box.listen { setValue(reverse(it)) }
        return box
    }

    fun <R : Any, S : Any> mapWith(
        other: Box<S>,
        reducer: (T, S) -> R,
        result: Box<R>? = null,
    ): Box<R> {
        return with(other).map({ (t, s) -> reducer(t, s) }, null, result)
    }

    fun <R : Any, S : Any> mapWith(
        other: Box<S>,
        reducer: (T, S) -> R,
        reverse: ((R) -> Pair<T, S>)? = null,
        result: Box<R>? = null,
    ): Box<R> {
        return with(other).map({ (t, s) -> reducer(t, s) }, reverse, result)
    }

    fun bindBidirectional(property: Property<T>) {
        property.addListener { _, _, newValue -> setValue(newValue) }
        listen { property.value = it }
    }
}
