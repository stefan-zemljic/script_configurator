package util

import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.BorderPane
import javafx.scene.shape.Rectangle

fun Node.frame(
    all: Number = 0,
    h: Number? = null,
    v: Number? = null,
    l: Number? = null,
    r: Number? = null,
    t: Number? = null,
    b: Number? = null,
): BorderPane {
    return BorderPane(this).apply {
        left = Rectangle((l ?: h ?: all).toDouble(), 0.0)
        right = Rectangle((r ?: h ?: all).toDouble(), 0.0)
        top = Rectangle(0.0, (t ?: v ?: all).toDouble())
        bottom = Rectangle(0.0, (b ?: v ?: all).toDouble())
    }
}