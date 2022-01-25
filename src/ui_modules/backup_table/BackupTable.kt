package ui_modules.backup_table

import backend.DBHandlerInterface.Backup
import javafx.scene.Node
import javafx.scene.control.SelectionMode
import util.async.Box
import util.custom_table.CustomTableView
import util.custom_table.XTableColumn
import java.text.SimpleDateFormat

fun buildBackupTable(state: BackupTableState): Node {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    return object : CustomTableView<Backup>(state.items, state.prefs, state.selectedBackups) {
        init {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
        }

        override fun buildColumns() {
            columns.add(XTableColumn.TextColumn("Timepoint") { Box(dateFormat.format(it.timepoint)) })
            columns.add(XTableColumn.TextColumn("Comment") { Box(it.comment) })
        }
    }
}