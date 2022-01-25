package util.custom_table

import backend.Preferences
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import util.async.Box

abstract class CustomTableView<T>(
    items: Box<List<T>>,
    prefs: Preferences,
    selectedItems: Box<List<T>>? = null,
    columnWidths: Box<List<Double>>? = null,
    onRowDoubleClick: ((T) -> Unit)? = null,
) : TableView<CustomTableView.Wrap<T>>() {
    init {
        isEditable = false
        items.listen { freshItems ->
            this.items = FXCollections.observableList(freshItems.map { Wrap(it) })
        }
        if (selectedItems == null) {
            selectionModel = null
        } else {
            selectionModel.selectedItems.addListener(ListChangeListener {
                selectedItems.setValue(selectionModel.selectedItems.map { it.value }.toList())
            })
        }
        this.buildColumns()
        val widthsBox = columnWidths ?: Box(emptyList())
        widthsBox.setValue(
            (prefs.get("${javaClass.name.hashCode()}_column_widths") ?: "")
                .split("\n")
                .mapNotNull { it.toDoubleOrNull() })
        val widths = widthsBox.value
        columns.withIndex().forEach { (i, column) ->
            widths.getOrNull(i)?.let { column.prefWidth = it }
            column.minWidth = 60.0
            column.widthProperty().addListener { _, _, _ -> widthsBox.setValue(columns.map { it.width }) }
        }
        widthsBox.setValue(columns.map { it.width })
        widthsBox.listen { list ->
            prefs.put(
                "${javaClass.name.hashCode()}_column_widths",
                list.joinToString("\n") { "${it.toInt()}" }
            )
        }
        onRowDoubleClick?.let { callback ->
            setRowFactory {
                TableRow<Wrap<T>>().apply {
                    setOnMouseClicked { event ->
                        if (event.clickCount == 2 && !isEmpty) {
                            callback(item.value)
                        }
                    }
                }
            }
        }
    }

    abstract fun buildColumns()

    class Wrap<T>(val value: T) {
        private val node = Object()

        override fun equals(other: Any?): Boolean {
            return other is Wrap<*> && node == other.node
        }

        override fun hashCode(): Int {
            return node.hashCode()
        }
    }
}
