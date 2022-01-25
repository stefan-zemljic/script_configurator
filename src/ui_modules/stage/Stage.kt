package ui_modules.stage

import backend.Preferences.Companion.PREF_STAGE_HEIGHT
import backend.Preferences.Companion.PREF_STAGE_WIDTH
import backend.Preferences.Companion.PREF_STAGE_X
import backend.Preferences.Companion.PREF_STAGE_Y
import event.Event
import javafx.beans.value.ObservableValue
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.text.Font
import javafx.stage.Modality
import javafx.stage.Stage
import ui_modules.backup_tab.BackupTabState
import ui_modules.backup_tab.buildBackupTab
import ui_modules.batch_tab.BatchTabState
import ui_modules.batch_tab.buildBatchTab
import ui_modules.dialogs.showCreateBatchesDialog
import ui_modules.dialogs.showEditEnvironmentDialog
import ui_modules.dialogs.showEditParameterDialog
import ui_modules.parameter_tab.ParameterTabState
import ui_modules.parameter_tab.buildParameterTab
import util.frame

fun initStage(stage: Stage) {
    with(stage) {
        val state = StageState()
        title = "StartScript-Creator"
        scene = Scene(buildRoot(state))
        show()
        loadLocationAndSize(state)
        saveLocationAndSizeOnChange(state)
        setupDialogs(state, stage)
    }
}

private fun setupDialogs(state: StageState, stage: Stage) {
    state.messager.listen { event ->
        when (event) {
            is Event.EditParameter -> showEditParameterDialog(event, state, stage)
            is Event.EditEnvironment -> showEditEnvironmentDialog(event, state, stage)
            is Event.CreateBatches -> showCreateBatchesDialog(stage, state, event)
            is Event.SuccessDialog -> {
                Alert(Alert.AlertType.INFORMATION).apply {
                    title = event.title
                    headerText = event.message
                    initOwner(stage)
                }.show()
            }
            is Event.ReadOnlyMonospaceDialog -> {
                Stage().apply {
                    title = event.title
                    initOwner(stage)
                    initModality(Modality.APPLICATION_MODAL)
                    scene = Scene(TextArea(event.text).apply {
                        font = Font.font("Monospaced")
                        isEditable = false
                    }.frame(10))
                }.show()
            }
            else -> Unit
        }
    }
}

private fun buildRoot(state: StageState): Parent {
    return TabPane().apply {
        tabs.add(
            Tab(
                "Parameters",
                buildParameterTab(ParameterTabState(state.dbHandler, state.pref, state.messager, state.isEditing))
            )
        )
        tabs.add(Tab("Batch", buildBatchTab(BatchTabState(state.dbHandler, state.isEditing, state.messager))))
        tabs.add(Tab("Backups", buildBackupTab(BackupTabState(state.dbHandler, state.pref, state.messager))))

        for (tab in tabs) tab.isClosable = false

        state.isEditing.listen {
            for (tab in tabs) {
                tab.isDisable = it && !tab.isSelected
            }
        }

        state.isLoading.listen { isDisable = it }
    }
}

private fun Stage.loadLocationAndSize(state: StageState) {
    val pairs = listOf<Pair<String, (Double) -> Unit>>(
        Pair(PREF_STAGE_HEIGHT) { height = it },
        Pair(PREF_STAGE_WIDTH) { width = it },
        Pair(PREF_STAGE_X) { x = it },
        Pair(PREF_STAGE_Y) { y = it },
    )

    for ((key, setter) in pairs) {
        setter(state.pref.getDouble(key) ?: continue)
    }
}

private fun Stage.saveLocationAndSizeOnChange(state: StageState) {
    fun <T> listenAndFilter(value: () -> ObservableValue<T>, condition: () -> Boolean, handler: (T) -> Unit) {
        value().addListener { _, _, newValue -> if (condition()) handler(newValue) }
    }

    listenAndFilter({ maximizedProperty() }, { isMaximized }) {
        state.pref.put(PREF_STAGE_X, 0)
        state.pref.put(PREF_STAGE_Y, 0)
    }

    listenAndFilter({ widthProperty() }, { !isMaximized }) { state.pref.put(PREF_STAGE_WIDTH, it.toInt()) }
    listenAndFilter({ heightProperty() }, { !isMaximized }) { state.pref.put(PREF_STAGE_HEIGHT, it.toInt()) }
    listenAndFilter({ xProperty() }, { !isMaximized }) { state.pref.put(PREF_STAGE_X, it.toInt()) }
    listenAndFilter({ yProperty() }, { !isMaximized }) { state.pref.put(PREF_STAGE_Y, it.toInt()) }
}