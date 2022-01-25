package util.custom_table

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.Node
import javafx.scene.control.*
import util.async.Box
import util.custom_table.CustomTableView.Wrap

sealed class XTableColumn<T>(title: String) : TableColumn<Wrap<T>, Wrap<T>>(title) {
    class ButtonColumn<T>(
        title: String,
        private val name: (T) -> Box<String>,
        private val onClick: ((T) -> Unit)? = null,
        private val isDisabled: ((T) -> Box<Boolean>)? = null,
    ) : XTableColumn<T>(title) {
        override fun buildNode(item: T): Node {
            return Button().apply {
                name(item).listen { text = it }
                this@ButtonColumn.isDisabled?.invoke(item)?.listen { isDisable = it }
                if (onClick != null) setOnAction { onClick.invoke(item) }
                maxWidth = Double.MAX_VALUE
            }
        }
    }

    class TextColumn<T>(
        title: String,
        private val text: (T) -> Box<String>,
    ) : XTableColumn<T>(title) {
        override fun buildNode(item: T): Node {
            return Label().apply {
                text(item).listen { text = it }
            }
        }
    }

    class ComboColumn<T>(
        title: String,
        private val value: (T) -> Box<String>,
        private val promptText: (T) -> Box<String>,
        private val suggestions: (T) -> Box<List<String>>,
        private val onAction: (T) -> Unit,
    ) : XTableColumn<T>(title) {
        override fun buildNode(item: T): Node {
            return ComboBox<String>().apply {
                isEditable = true
                this@ComboColumn.promptText(item).bindBidirectional(promptTextProperty())
                value(item).bindBidirectional(editor.textProperty())
                suggestions(item).listen { items = FXCollections.observableList(it) }
                maxWidth = Double.MAX_VALUE
                setOnAction { this@ComboColumn.onAction(item) }
                editor.focusedProperty().addListener { _, _, isFocused ->
                    if (isFocused) {
                        Platform.runLater {
                            editor.selectAll()
                        }
                    }
                }
            }
        }
    }

    init {
        isSortable = false
        isReorderable = false
        setCellValueFactory { SimpleObjectProperty(it.value) }
        setCellFactory {
            object : TableCell<Wrap<T>, Wrap<T>>() {
                override fun updateItem(item: Wrap<T>?, empty: Boolean) {
                    super.updateItem(item, empty)
                    graphic = if (item == null) null else buildNode(item.value)
                }
            }
        }
    }

    protected abstract fun buildNode(item: T): Node
}