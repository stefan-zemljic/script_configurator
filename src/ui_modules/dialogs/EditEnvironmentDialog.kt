package ui_modules.dialogs

import event.Event
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import ui_modules.stage.StageState
import util.async.Box
import util.frame

fun showEditEnvironmentDialog(
    event: Event.EditEnvironment,
    state: StageState,
    stage: Stage
) {
    val environment = Box(event.environment?.data?.get(0) ?: "")
    val name = Box(event.environment?.data?.get(1) ?: "")
    val database = Box(event.environment?.data?.get(2) ?: "")
    Stage().apply {
        title = if (event.environment == null) "New environment" else "Edit environment"
        initOwner(stage)
        initModality(Modality.APPLICATION_MODAL)
        scene = Scene(
            VBox(
                10.0,
                TextField().apply {
                    promptText = "Environment"
                    environment.bindBidirectional(textProperty())
                },
                TextField().apply {
                    promptText = "Name"
                    name.bindBidirectional(textProperty())
                },
                TextField().apply {
                    promptText = "Database"
                    database.bindBidirectional(textProperty())
                },
                HBox(10.0).apply {
                    alignment = Pos.CENTER

                    children.add(Button("Cancel").apply {
                        setOnAction {
                            hide()
                        }
                    })

                    if (event.environment != null) {
                        children.add(Button("Delete").apply {
                            setOnAction {
                                state.dbHandler.deleteEnvironment(event.environment)
                                state.messager.send(Event.EnvironmentsChanged)
                                hide()
                            }
                        })
                    }

                    children.add(Button("Save").apply {
                        setOnAction {
                            if (event.environment == null) {
                                state.dbHandler.addEnvironment(
                                    "${System.currentTimeMillis()}",
                                    listOf(environment.value, name.value, database.value),
                                    true
                                )
                            } else {
                                state.dbHandler.updateEnvironment(
                                    event.environment.id,
                                    listOf(environment.value, name.value, database.value),
                                    true,
                                )
                            }
                            state.messager.send(Event.EnvironmentsChanged)
                            hide()
                        }
                    })
                    alignment = Pos.CENTER
                }
            ).frame(10)
        )
    }.show()
}