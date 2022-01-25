package ui_modules

import javafx.application.Application
import javafx.stage.Stage
import ui_modules.stage.initStage

class App : Application() {
    override fun start(stage: Stage) {
        initStage(stage)
    }
}
