package ui_modules.dialogs

import backend.DBHandler
import backend.DBHandlerInterface
import backend.DBHandlerInterface.Parameter
import event.Event
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import ui_modules.stage.StageState
import util.async.Messager
import util.frame
import java.nio.file.Files
import java.nio.file.Path

fun showCreateBatchesDialog(stage: Stage, state: StageState, event: Event.CreateBatches) {
    val scriptNameParameters = state.dbHandler.getParameters().filter { it.isScriptName }
    val selectedParameters = FXCollections.observableHashMap<Parameter, Unit>()
    Stage().apply {
        title = "Create Batches"
        initOwner(stage)
        initModality(Modality.APPLICATION_MODAL)
        scene = Scene(
            VBox(
                10.0,
                HBox(
                    VBox(10.0).apply {
                        for (parameter in scriptNameParameters) {
                            children.add(CheckBox(parameter.name).apply {
                                selectedProperty().addListener { _, _, selected ->
                                    if (selected) {
                                        selectedParameters[parameter] = Unit
                                    } else {
                                        selectedParameters.remove(parameter)
                                    }
                                }
                                isSelected = true
                            })
                        }
                    },
                ).apply {
                    alignment = Pos.CENTER
                },
                HBox(
                    10.0,
                    Button("Cancel").apply {
                        setOnAction { hide() }
                    },
                    Button("Create Batches").apply {
                        selectedParameters.addListener(MapChangeListener {
                            isDisable = selectedParameters.isEmpty()
                        })
                        setOnAction {
                            createBatches(
                                state.dbHandler,
                                event.selectedEnvironments,
                                selectedParameters.keys.toList(),
                                state.messager,
                            )
                            hide()
                        }
                    },
                ).apply {
                    alignment = Pos.CENTER
                }
            ).apply {
                alignment = Pos.CENTER
            }.frame(10)
        )
    }.show()
}

private fun createBatches(
    dbHandler: DBHandlerInterface,
    environments: List<DBHandlerInterface.Environment>,
    scriptNames: List<Parameter>,
    messager: Messager<Event>,
) {
    val parameters = dbHandler.getParameters().associateWith { dbHandler.getValues(it).toMap() }

    fun applyReplacements(environment: DBHandlerInterface.Environment, text: String): String {
        var t = text
        for ((parameter, values) in parameters) {
            t = t.replace("%${parameter.name}%", values[environment.id] ?: "")
        }
        t = t.replace("%Environment%", environment.data[0])
        t = t.replace("%Name%", environment.data[1])
        t = t.replace("%Database%", environment.data[2])
        return t
    }

    for (parameter in scriptNames) {
        val failed = mutableListOf<Pair<DBHandlerInterface.Environment, String>>()
        val created = mutableListOf<Pair<DBHandlerInterface.Environment, String>>()
        for (environment in environments) {
            val batch = applyReplacements(environment, parameter.batchText)
            val scriptName = applyReplacements(environment, parameters[parameter]!![environment.id] ?: "")
            try {
                if (scriptName.isEmpty()) {
                    throw Exception("No script name given")
                }
                val path = Path.of(scriptName)
                Files.writeString(path, batch)
                created.add(Pair(environment, path.toRealPath().toString()))
            } catch (e: Exception) {
                System.err.println("Batch creation for $environment failed:")
                e.printStackTrace()
                failed.add(Pair(environment, e.message ?: ""))
            }
        }

        val builder = StringBuilder()
        builder.append("Result for $parameter\n")
        if (failed.isNotEmpty()) {
            builder.append("Batch creation failed for:\n")
            for ((environment, message) in failed) {
                builder.append("  ")
                builder.append(environment)
                builder.append(" (")
                builder.append(message)
                builder.append(")\n")
            }
        }
        if (created.isEmpty()) {
            builder.append("No batches created")
        } else {
            builder.append("Batch created for:\n")
            for ((environment, path) in created) {
                builder.append("  ")
                builder.append(environment)
                builder.append(" -> ")
                builder.append(path)
                builder.append("\n")
            }
        }
        messager.send(Event.SuccessDialog.BatchesCreated("Done", builder.toString()))
    }
}
