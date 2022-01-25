package ui_modules.batch_tab

import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import util.frame
import javax.swing.JComboBox

fun buildBatchTab(state: BatchTabState): Node {
    return VBox(
        10.0,
        ComboBox<String>().apply {
            state.isEditing.listen { isDisable = it }
            state.scriptNameParameters.listen { parameters ->
                var selectedIndex = selectionModel.selectedIndex
                val parameterNames = parameters.map { it.name }
                if (items == null) {
                    items = FXCollections.observableList(parameterNames)
                } else {
                    items.setAll(parameterNames)
                }
                if (items.size == 0) {
                    selectionModel.select(-1)
                    state.parameterSelected(-1)
                }else {
                    if (selectedIndex == -1 || selectedIndex >= items.size) {
                        selectedIndex = 0
                    }
                    selectionModel.select(selectedIndex)
                    state.parameterSelected(selectedIndex)
                }
            }
            selectionModel.selectedIndexProperty().addListener { _, _, index ->
                state.parameterSelected(index.toInt())
            }
        },
        TextArea().apply {
            VBox.setVgrow(this, Priority.ALWAYS)
            promptText = "Batch"
            state.batchTextInUI.bindBidirectional(textProperty())
        },
        HBox(
            10.0,
            Button("Discard changes").apply {
                setOnAction { state.discardBatchText() }
            },
            Button("Save changes").apply {
                setOnAction { state.saveBatchText() }
            }
        ).apply {
            alignment = Pos.CENTER
            state.isEditing.listen { isDisable = !it }
        }
    ).frame(10)
}