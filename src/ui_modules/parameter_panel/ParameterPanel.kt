package ui_modules.parameter_panel

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ui_modules.parameter_table.ParameterTableState
import ui_modules.parameter_table.buildParameterTable

fun buildParameterPanel(state: ParameterPanelState): Node {
    return VBox(
        10.0,
        buildParameterTable(
            ParameterTableState(
                state.dbHandler,
                state.prefs,
                state.selectedEnvironments,
                state.isEditing,
                state.messager,
            )
        ).apply {
            VBox.setVgrow(this, Priority.ALWAYS)
        },
        FlowPane(
            10.0,
            10.0,
            Button("New parameter").apply {
                state.isEditing.listen { isDisable = it }
                setOnAction { state.newParameter() }
            },
            HBox(
                10.0,
                Button("Copy values").apply {
                    state.selectedEnvironments.listen { isDisable = it.isEmpty() }
                    setOnAction { state.copyValues() }
                },
                Button("Paste values").apply {
                    setOnAction { state.pasteValues() }
                    state.selectedEnvironments.with(state.copiedValues).listen { (environments, values) ->
                        isDisable = environments.isEmpty() || values.isEmpty()
                    }
                }
            ),
            HBox(
                10.0,
                Button("Discard changes").apply {
                    setOnAction { state.discardChanges() }
                },
                Button().apply {
                    state.selectedEnvironments.listen {
                        text = when (it.size) {
                            0, 1 -> "Save changes"
                            else -> "Save changes for ${it.size} environments"
                        }
                    }
                    setOnAction { state.saveChanges() }
                },
            ).apply {
                state.isEditing.listen { isDisable = !it }
            },
        ).apply {
            alignment = Pos.CENTER
        }
    ).apply {
        VBox.setVgrow(this, Priority.ALWAYS)
    }
}