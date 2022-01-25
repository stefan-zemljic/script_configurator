package ui_modules.backup_tab

import backend.DBHandlerInterface
import backend.DBHandlerInterface.Backup
import backend.Preferences
import event.Event
import event.Event.BackupsChanged
import event.Event.SuccessDialog.BackupRestored
import util.async.Box
import util.async.Messager

class BackupTabState(
    internal val dbHandler: DBHandlerInterface,
    internal val prefs: Preferences,
    internal val messager: Messager<Event>,
) {
    internal val commentText = Box("")
    internal val selectedBackups = Box(listOf<Backup>())

    internal fun createBackup() {
        dbHandler.addBackup(commentText.value)
        commentText.setValue("")
        messager.send(BackupsChanged)
    }

    internal fun restoreBackup() {
        dbHandler.restoreBackup(selectedBackups.value.first())
        messager.send(BackupRestored)
    }

    internal fun deleteBackups() {
        selectedBackups.value.forEach(dbHandler::deleteBackup)
        messager.send(BackupsChanged)
    }
}