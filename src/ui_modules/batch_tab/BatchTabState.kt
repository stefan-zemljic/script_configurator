package ui_modules.batch_tab

import backend.DBHandlerInterface
import backend.DBHandlerInterface.Parameter
import event.Event
import util.async.Box
import util.async.Messager

class BatchTabState(
    private val dbHandler: DBHandlerInterface,
    internal val isEditing: Box<Boolean>,
    private val messager: Messager<Event>
) {
    internal val batchTextInUI = Box("")
    internal val scriptNameParameters = Box(emptyList<Parameter>())
    private var chosenParameter = Box(emptyList<Parameter>())

    init {
        chosenParameter.with(batchTextInUI).listen { (a, b) ->
            if (scriptNameParameters.value.isNotEmpty()) {
                isEditing.setValue(a.firstOrNull()?.batchText != b)
            }
        }
        messager.listen { if (it == Event.ParametersChanged) reloadParameters() }
        reloadParameters()
    }

    private fun reloadParameters() {
        scriptNameParameters.setValue(dbHandler.getParameters().filter { it.isScriptName })
    }

    internal fun saveBatchText() {
        chosenParameter.value.firstOrNull()?.let { parameter ->
            dbHandler.updateParameter(
                parameter,
                parameter.name,
                parameter.isScriptName,
                batchTextInUI.value
            )
            messager.send(Event.ParametersChanged)
        }
    }

    internal fun discardBatchText() {
        batchTextInUI.setValue(chosenParameter.value.firstOrNull()?.batchText ?: "")
    }

    internal fun parameterSelected(index: Int) {
        if (index == -1) {
            chosenParameter.setValue(emptyList())
        } else {
            val parameter = scriptNameParameters.value[index]
            chosenParameter.setValue(listOf(parameter))
            batchTextInUI.setValue(parameter.batchText)
        }
    }
}