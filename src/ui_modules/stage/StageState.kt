package ui_modules.stage

import backend.DBHandler
import backend.DBHandlerInterface
import backend.Preferences
import event.Event
import util.async.Box
import util.async.Messager

class StageState {
    internal val dbHandler = DBHandler() as DBHandlerInterface
    internal val pref = Preferences()
    internal val messager = Messager<Event>()

    internal val isLoading = Box(false)
    internal val isEditing = Box(false)
}

