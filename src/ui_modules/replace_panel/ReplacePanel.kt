package ui_modules.replace_panel

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import util.frame

fun buildReplacePanel(state: ReplacePanelState): Node {
    return VBox(
        10.0,
        TextField().apply {
            promptText = "Search"
            state.searchText.bindBidirectional(textProperty())
        },
        TextField().apply {
            promptText = "Replace"
            state.replaceText.bindBidirectional(textProperty())
        },
        FlowPane(
            10.0, 10.0,
            Button("Preview replacements").apply {
                state.matchCount.listen {
                    isDisable = it == 0

                    setOnAction { state.previewReplacements() }
                }
            },
            Button().apply {
                state.selectedEnvironments.with(state.matchCount).listen { (environments, matchCount) ->
                    isDisable = matchCount == 0

                    text = when {
                        matchCount == 0 -> "Replace"
                        environments.size == 1 -> "Replace $matchCount matches in this environment"
                        else -> {
                            val environmentCount = environments.size
                            val ratio = "%.2f per environment".format(matchCount / environmentCount.toDouble())
                            "Replace $matchCount matches in $environmentCount environments ($ratio)"
                        }
                    }
                }

                setOnAction { state.replace() }
            },
        ).apply {
            alignment = Pos.CENTER
        },
    ).apply {
        alignment = Pos.CENTER
    }
}