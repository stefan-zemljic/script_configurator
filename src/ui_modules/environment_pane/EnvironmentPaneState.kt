package ui_modules.environment_pane

import backend.DBHandlerInterface
import backend.DBHandlerInterface.*
import backend.Preferences
import event.Event
import event.Event.SuccessDialog.BackupRestored
import javafx.stage.Stage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import util.async.Box
import util.async.Messager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.text.StringBuilder

class EnvironmentPaneState(
    private val dbHandler: DBHandlerInterface,
    internal val prefs: Preferences,
    private val messager: Messager<Event>,
    internal val selectedEnvironments: Box<List<Environment>>,
    tableWidth: Box<Double>,
) {
    internal val items = Box(emptyList<Environment>())
    internal val columnWidths = Box<List<Double>>(emptyList())

    init {
        messager.listen { if (it is BackupRestored || it is Event.EnvironmentsChanged) refreshItems() }
        columnWidths.listen { tableWidth.setValue(columnWidths.value.fold(0.0) { a, b -> a + b }) }
        refreshItems()
    }

    private fun refreshItems() {
        items.setValue(dbHandler.getEnvironments().sortedBy { it.data[1] })
    }

    internal fun reloadEnvironments() {
        // Code removed for privacy reasons (reloads environments from web API)
    }

    internal fun createBatches() {
        messager.send(Event.CreateBatches(selectedEnvironments.value))
    }

    fun editEnvironment(environment: Environment) {
        messager.send(Event.EditEnvironment(environment))
    }

    fun addEnvironment() {
        messager.send(Event.EditEnvironment(null))
    }
}