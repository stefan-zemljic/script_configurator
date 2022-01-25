package ui_modules.environment_pane

import backend.DBHandlerInterface.Environment
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.SelectionMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import util.async.Box
import util.custom_table.CustomTableView
import util.custom_table.XTableColumn

fun buildEnvironmentPane(state: EnvironmentPaneState): Node {
    return VBox(
        10.0,
        object : CustomTableView<Environment>(
            state.items,
            state.prefs,
            state.selectedEnvironments,
            state.columnWidths
        ) {
            init {
                selectionModel.selectionMode = SelectionMode.MULTIPLE
            }

            override fun buildColumns() {
                for ((index, header) in listOf("Environment", "Name", "Database").withIndex()) {
                    columns.add(
                        XTableColumn.TextColumn(header) { Box(it.data[index]) }
                    )
                }
                columns.add(
                    XTableColumn.ButtonColumn<Environment>(
                        "Edit",
                        { Box("Edit") },
                        { state.editEnvironment(it) },
                    )
                )
            }
        }.apply {
            VBox.setVgrow(this, Priority.ALWAYS)
        },
        HBox(
            10.0,
            Button("Reload environments").apply {
                isDisable = true
                setOnAction { state.reloadEnvironments() }
            },
            Button("Add environment").apply {
                setOnAction { state.addEnvironment() }
            },
            Button().apply {
                state.selectedEnvironments.listen {
                    text = when (it.size) {
                        0, 1 -> "Create batch"
                        else -> "Create ${it.size} batches"
                    }

                    setOnAction { state.createBatches() }
                }
            },
        ).apply {
            alignment = Pos.CENTER
        },
    )
}