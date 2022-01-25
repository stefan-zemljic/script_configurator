package ui_modules.parameter_tab

import backend.DBHandlerInterface
import backend.Preferences
import event.Event
import util.async.Box
import util.async.Messager

class ParameterTabState(
    internal val dbHandler: DBHandlerInterface,
    internal val prefs: Preferences,
    internal val messager: Messager<Event>,
    internal val isEditing: Box<Boolean>,
) {
    internal val selectedEnvironments = Box(listOf<DBHandlerInterface.Environment>())
    internal val tableWidth = Box(0.0)
}
