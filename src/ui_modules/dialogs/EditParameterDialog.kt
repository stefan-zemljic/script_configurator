package ui_modules.dialogs

import event.Event
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import ui_modules.stage.StageState
import util.async.Box
import util.frame

fun showEditParameterDialog(
    event: Event.EditParameter,
    state: StageState,
    stage: Stage
) {
    val name = Box(event.parameter?.name ?: "")
    val isScriptName = Box(event.parameter?.isScriptName == true)
    val parameters = state.dbHandler.getParameters().map { it.name }.toSet()
    Stage().apply {
        title = if (event.parameter == null) "New parameter" else "Edit parameter"
        initOwner(stage)
        initModality(Modality.APPLICATION_MODAL)
        scene = Scene(
            VBox(
                10.0,
                TextField().apply {
                    promptText = "Name"
                    name.bindBidirectional(textProperty())
                },
                CheckBox("Is Script Name").apply {
                    isScriptName.bindBidirectional(this.selectedProperty())
                },
                HBox(10.0).apply {
                    alignment = Pos.CENTER

                    children.add(Button("Cancel").apply {
                        setOnAction {
                            hide()
                        }
                    })

                    if (event.parameter != null) {
                        children.add(Button("Delete").apply {
                            setOnAction {
                                state.dbHandler.deleteParameter(event.parameter)
                                state.messager.send(Event.ParametersChanged)
                                hide()
                            }
                        })
                    }

                    children.add(Button("Save").apply {
                        name.with(isScriptName).listen { (name, isScriptName) ->
                            isDisable = when {
                                name.isEmpty() -> true
                                name == event.parameter?.name -> isScriptName == event.parameter.isScriptName
                                else -> parameters.contains(name)
                            }
                        }
                        name.listen { isDisable = it.isEmpty() || parameters.contains(it) }
                        setOnAction {
                            if (event.parameter == null) {
                                state.dbHandler.addParameter(name.value, isScriptName.value)
                            } else {
                                state.dbHandler.updateParameter(event.parameter, name.value, isScriptName.value, event.parameter.batchText)
                            }
                            state.messager.send(Event.ParametersChanged)
                            hide()
                        }
                    })
                    alignment = Pos.CENTER
                }
            ).frame(10)
        )
    }.show()
}