package ui_modules.backup_table

import backend.DBHandlerInterface
import backend.DBHandlerInterface.Backup
import backend.Preferences
import event.Event
import event.Event.BackupsChanged
import util.async.Box
import util.async.Messager

class BackupTableState(
    private val dbHandler: DBHandlerInterface,
    internal val prefs: Preferences,
    messager: Messager<Event>,
    internal val selectedBackups: Box<List<Backup>>
) {
    val items = Box(emptyList<Backup>())

    init {
        messager.listen { if (it is BackupsChanged) refreshItems() }
        refreshItems()
    }

    private fun refreshItems() {
        items.setValue(dbHandler.getBackups())
    }
}