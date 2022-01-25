package event

import backend.DBHandlerInterface.Environment
import backend.DBHandlerInterface.Parameter

sealed class Event {
    object BackupsChanged : Event()

    class ReadOnlyMonospaceDialog(val title: String, val text: String) : Event()

    sealed class SuccessDialog(val title: String, val message: String) : Event() {
        class BatchesCreated(title: String, message: String) : SuccessDialog(title, message)
        object ParameterValuesChanged : SuccessDialog("Replaced", "Successfully applied replacements")
        object BackupRestored : SuccessDialog("Restored", "Backup successfully restored")
        object EnvironmentsReloaded : SuccessDialog("Reloaded", "Environments successfully reloaded")
        class EnvironmentReloadFailed(message: String) : SuccessDialog("Reloading failed", message)
    }

    class EditEnvironment(val environment: Environment?): Event()
    object EnvironmentsChanged : Event()

    class EditParameter(val parameter: Parameter?) : Event()
    object ParametersChanged : Event()

    class RequestParameterValues(val callback: (parameterValues: Map<Parameter, String>) -> Unit) : Event()
    class PasteParameterValues(val copiedValues: Map<Parameter, String>) : Event()

    class CreateBatches(val selectedEnvironments: List<Environment>): Event()

    object DiscardParameterChanges : Event()
    object SaveParameterValues : Event()

}