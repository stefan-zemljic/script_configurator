package ui_modules.replace_panel

import backend.DBHandlerInterface
import backend.DBHandlerInterface.Environment
import backend.DBHandlerInterface.Parameter
import event.Event
import event.Event.ReadOnlyMonospaceDialog
import event.Event.SuccessDialog
import util.async.Box
import util.async.Messager
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class ReplacePanelState(
    private val dbHandler: DBHandlerInterface,
    internal val selectedEnvironments: Box<List<Environment>>,
    private val messager: Messager<Event>,
) {
    internal val searchText = Box("")
    internal val replaceText = Box("")
    internal val matchCount = Box(0)
    private val replacers = ArrayList<Pair<Triple<Environment, Parameter, String>, List<Pair<Int, Int>>>>()

    init {
        searchText.with(selectedEnvironments).listen { (search, environments) ->
            replacers.clear()
            val pattern = try {
                Pattern.compile(search)
            } catch (e: PatternSyntaxException) {
                matchCount.setValue(0)
                return@listen
            }
            var count = 0
            for (parameter in dbHandler.getParameters()) {
                val values = dbHandler.getValues(parameter).toMap()
                for (environment in environments) {
                    val value = values[environment.id] ?: ""
                    val matcher = pattern.matcher(value)
                    if (matcher.find()) {
                        val matches = ArrayList<Pair<Int, Int>>()
                        do {
                            matches.add(Pair(matcher.start(), matcher.end()))
                        } while (matcher.find())
                        count += matches.size
                        replacers.add(Pair(Triple(environment, parameter, value), matches))
                    }
                }
            }
            matchCount.setValue(count)
        }
    }

    private fun computeReplaced(): List<Pair<Pair<Environment, Parameter>, Pair<String, String>>> {
        val replacement = replaceText.value
        return replacers.map { (meta, ranges) ->
            var (_, _, newValue) = meta
            for ((start, end) in ranges.reversed()) {
                newValue = newValue.replaceRange(start, end, replacement)
            }
            Pair(Pair(meta.first, meta.second), Pair(meta.third, newValue))
        }
    }

    internal fun replace() {
        computeReplaced().forEach { (meta, values) ->
            val (environment, parameter) = meta
            val (_, newValue) = values
            dbHandler.putValue(environment, parameter, newValue)
        }
        val before = searchText.value
        searchText.setValue("")
        searchText.setValue(before)
        messager.send(SuccessDialog.ParameterValuesChanged)
    }

    internal fun previewReplacements() {
        val builder = StringBuilder()
        for ((environment, data) in computeReplaced().groupBy { it.first.first }) {
            builder.append("${environment.data.joinToString { it }}\n")
            for ((meta, values) in data) {
                val (_, parameter) = meta
                val (oldValue, newValue) = values
                builder.append("  ${parameter.name}\n")
                builder.append("    old: $oldValue\n")
                builder.append("    new: $newValue\n")
            }
        }
        messager.send(ReadOnlyMonospaceDialog("Preview", builder.toString()))
    }
}