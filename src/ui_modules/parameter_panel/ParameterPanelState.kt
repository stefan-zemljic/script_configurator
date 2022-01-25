package ui_modules.parameter_panel

import backend.DBHandlerInterface
import backend.DBHandlerInterface.Environment
import backend.DBHandlerInterface.Parameter
import backend.Preferences
import event.Event
import util.async.Box
import util.async.Messager

class ParameterPanelState(
    internal val dbHandler: DBHandlerInterface,
    internal val prefs: Preferences,
    internal val selectedEnvironments: Box<List<Environment>>,
    internal val messager: Messager<Event>,
    internal val isEditing: Box<Boolean>,
) {
    internal val copiedValues = Box(emptyMap<Parameter, String>())

    fun discardChanges() {
        messager.send(Event.DiscardParameterChanges)
    }

    fun saveChanges() {
        messager.send(Event.SaveParameterValues)
    }

    fun pasteValues() {
        messager.send(Event.PasteParameterValues(copiedValues.value))
    }

    fun copyValues() {
        messager.send(Event.RequestParameterValues { copiedValues.setValue(it) })
    }

    fun newParameter() {
        messager.send(Event.EditParameter(null))
    }
}