package ui_modules.backup_tab

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import ui_modules.backup_table.BackupTableState
import ui_modules.backup_table.buildBackupTable
import util.frame

fun buildBackupTab(state: BackupTabState): Node {
    return VBox(
        10.0,
        TextField().apply {
            promptText = "Comment"
            state.commentText.bindBidirectional(textProperty())
        },
        Button("Create backup").apply {
            setOnAction { state.createBackup() }
        },
        buildBackupTable(BackupTableState(state.dbHandler, state.prefs, state.messager, state.selectedBackups)).apply {
            VBox.setVgrow(this, Priority.ALWAYS)
        },
        HBox(
            10.0,
            Button("Delete backup[s]").apply {
                state.selectedBackups.listen {
                    text = when (it.size) {
                        0, 1 -> "Delete backup"
                        else -> "Delete ${it.size} backups"
                    }
                    isDisable = it.isEmpty()
                }
                setOnAction { state.deleteBackups() }
            },
            Button("Restore backup").apply {
                state.selectedBackups.listen { isDisable = it.size != 1 }
                setOnAction { state.restoreBackup() }
            },
        ).apply {
            alignment = Pos.CENTER
        },
    ).apply {
        alignment = Pos.CENTER
    }.frame(10)
}
