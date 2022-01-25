package ui_modules.parameter_tab

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import ui_modules.environment_pane.EnvironmentPaneState
import ui_modules.environment_pane.buildEnvironmentPane
import ui_modules.parameter_panel.ParameterPanelState
import ui_modules.parameter_panel.buildParameterPanel
import ui_modules.replace_panel.ReplacePanelState
import ui_modules.replace_panel.buildReplacePanel
import util.frame

fun buildParameterTab(state: ParameterTabState): Node {
    return BorderPane().apply {
        left = buildEnvironmentPane(
            EnvironmentPaneState(
                state.dbHandler,
                state.prefs,
                state.messager,
                state.selectedEnvironments,
                state.tableWidth,
            )
        ).frame(r = 10).apply {
            state.tableWidth.listen {
                minWidth = state.tableWidth.value + 30
                maxWidth = state.tableWidth.value + 30
            }
        }.apply {
            state.isEditing.listen { isDisable = it }
        }

        center = VBox(10.0).apply {
            alignment = Pos.CENTER
            children.add(
                buildReplacePanel(
                    ReplacePanelState(
                        state.dbHandler,
                        state.selectedEnvironments,
                        state.messager,
                    )
                ).apply {
                    state.isEditing.listen { isDisable = it }
                }
            )

            children.add(
                buildParameterPanel(
                    ParameterPanelState(
                        state.dbHandler,
                        state.prefs,
                        state.selectedEnvironments,
                        state.messager,
                        state.isEditing,
                    )
                )
            )
        }
    }.frame(10).apply {
        maxWidth = Double.MAX_VALUE
    }
}