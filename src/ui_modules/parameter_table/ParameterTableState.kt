package ui_modules.parameter_table

import backend.DBHandlerInterface
import backend.DBHandlerInterface.Environment
import backend.DBHandlerInterface.Parameter
import backend.Preferences
import event.Event
import event.Event.SuccessDialog.BackupRestored
import event.Event.SuccessDialog.ParameterValuesChanged
import util.async.Box
import util.async.Messager
import kotlin.math.min

class ParameterTableState(
    private val dbHandler: DBHandlerInterface,
    internal val prefs: Preferences,
    private val selectedEnvironments: Box<List<Environment>>,
    internal val isEditing: Box<Boolean>,
    private val messager: Messager<Event>,
) {
    internal val parameters = Box<List<Parameter>>(emptyList())
    private val initialValues = HashMap<Parameter, Box<String>>()
    internal val parameterValues = HashMap<Parameter, Box<String>>()
    internal val parameterSuggestions = HashMap<Parameter, Box<List<String>>>()
    internal val isUnchanged = HashMap<Parameter, Box<Boolean>>()

    init {
        selectedEnvironments.listen {
            if (parameters.value.isEmpty() != it.isEmpty()) {
                reinitializeData()
            } else {
                updateValues(parameters.value)
            }
        }
        messager.listen(::handleEvent)
        reinitializeData()
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is Event.DiscardParameterChanges -> {
                for (parameter in parameters.value) {
                    parameterValues[parameter]!!.setValue(initialValues[parameter]!!.value)
                }
            }
            is Event.PasteParameterValues -> {
                if (parameters.value.isNotEmpty()) {
                    for ((parameter, value) in event.copiedValues) {
                        parameterValues[parameter]?.setValue(value)
                    }
                }
            }
            is Event.RequestParameterValues -> {
                if (parameters.value.isEmpty()) event.callback(emptyMap())
                else event.callback(parameterValues.mapValues { it.value.value })
            }
            is Event.SaveParameterValues -> {
                for (parameter in parameters.value) {
                    val value = parameterValues[parameter]!!.value
                    if (value != initialValues[parameter]!!.value) {
                        for (environment in selectedEnvironments.value) {
                            dbHandler.putValue(environment, parameter, value)
                        }
                    }
                }
                reinitializeData()
            }
            is BackupRestored -> reinitializeData()
            is ParameterValuesChanged -> updateValues(parameters.value)
            is Event.ParametersChanged -> reinitializeData()
            else -> Unit
        }
    }

    private fun reinitializeData() {
        val parameters = if (selectedEnvironments.value.isEmpty()) emptyList()
        else dbHandler.getParameters().also { parameters ->
            updateValues(parameters)
            for (parameter in parameters) {
                parameterValues.getOrPut(parameter) {
                    initialValues[parameter]!!.map { it }
                }
                isUnchanged.getOrPut(parameter) {
                    initialValues[parameter]!!
                        .with(parameterValues[parameter]!!)
                        .map { (a, b) -> a == b }.apply {
                            listen {
                                isEditing.setValue(isUnchanged.any { !it.value.value })
                            }
                        }
                }
            }
        }
        this.parameters.setValue(parameters)
    }

    private fun updateValues(parameters: List<Parameter>) {
        for (parameter in parameters) {
            val values = dbHandler.getValues(parameter)
            parameterSuggestions.getOrPut(parameter) { Box(emptyList()) }.apply {
                setValue(values.map { it.second }.toSet().toList())
            }
            var first: String? = null
            for (environment in selectedEnvironments.value) {
                val value = values.find { it.first == environment.id }?.second ?: ""
                if (first == null || value == first) {
                    first = value
                } else {
                    first = null
                    break
                }
            }
            initialValues.getOrPut(parameter) { Box("") }.setValue(first ?: "Multiple values")
        }
    }

    internal fun revertParameterValue(parameter: Parameter) {
        parameterValues[parameter]!!.setValue(initialValues[parameter]!!.value)
    }

    internal fun editParameter(parameter: Parameter) {
        messager.send(Event.EditParameter(parameter))
    }

    internal fun saveChange(parameter: Parameter) {
        if (isUnchanged[parameter]!!.value) return
        val value = parameterValues[parameter]!!.value
        for (environment in selectedEnvironments.value) {
            dbHandler.putValue(environment, parameter, value)
        }
        initialValues[parameter]!!.setValue(value)
    }

    internal fun moveParameter(parameter: Parameter, fromIndex: Int, toIndex: Int) {
        parameters.setValue(parameters.value
            .toMutableList()
            .apply {
                removeAt(fromIndex)
                add(min(size, toIndex), parameter)
            })
        dbHandler.setParameterOrder(parameters.value.apply {
            forEachIndexed { index, parameter -> parameter.position = index }
        })
    }
}