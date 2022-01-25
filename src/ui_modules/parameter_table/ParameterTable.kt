package ui_modules.parameter_table

import backend.DBHandlerInterface.Parameter
import javafx.scene.Node
import javafx.scene.control.TableRow
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import util.async.Box
import util.custom_table.CustomTableView
import util.custom_table.XTableColumn

private val parameterFormat = DataFormat("ParameterIndex")

fun buildParameterTable(state: ParameterTableState): Node {
    return object : CustomTableView<Parameter>(
        state.parameters,
        state.prefs,
    ) {
        init {
            setRowFactory {
                TableRow<Wrap<Parameter>>().apply {
                    val row = this
                    setOnDragDetected {
                        val db = startDragAndDrop(TransferMode.MOVE)
                        db.dragView = snapshot(null, null)
                        db.setContent(ClipboardContent().apply {
                            put(parameterFormat, index)
                        })
                        it.consume()
                    }
                    setOnDragOver {
                        val db = it.dragboard
                        if (db.hasContent(parameterFormat)) {
                            if (index != db.getContent(parameterFormat)) {
                                it.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
                                it.consume()
                            }
                        }
                    }
                    setOnDragDropped {
                        val db = it.dragboard
                        if (db.hasContent(parameterFormat)) {
                            val index = db.getContent(parameterFormat) as Int
                            val parameter = state.parameters.value[index]
                            val dropIndex = if (isEmpty) items.size else row.index
                            state.moveParameter(parameter, index, dropIndex)
                            it.consume()
                        }
                    }
                }
            }
        }

        override fun buildColumns() {
            columns.add(
                XTableColumn.ButtonColumn(
                    "Parameter",
                    { Box(it.name) },
                    { state.editParameter(it) },
                )
            )

            columns.add(
                XTableColumn.ComboColumn(
                    "Value",
                    { state.parameterValues[it]!! },
                    { Box("Value") },
                    { state.parameterSuggestions[it]!! },
                    { state.saveChange(it) }
                )
            )

            columns.add(
                XTableColumn.ButtonColumn(
                    "Revert",
                    { Box("Revert") },
                    { state.revertParameterValue(it) },
                    { state.isUnchanged[it]!! },
                )
            )
        }
    }
}