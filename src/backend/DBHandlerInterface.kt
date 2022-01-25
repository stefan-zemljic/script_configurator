package backend

interface DBHandlerInterface : AutoCloseable {
    fun getInfo(key: String): String
    fun putInfo(key: String, value: Any?)
    fun getEnvironments(): List<Environment>
    fun addEnvironment(id: String, data: List<String>, manually: Boolean = false)
    fun updateEnvironment(id: String, data: List<String>, manually: Boolean = false)
    fun deleteEnvironment(environment: Environment)
    fun getParameters(): List<Parameter>
    fun addParameter(name: String, isScriptName: Boolean)
    fun updateParameter(parameter: Parameter, name: String, isScriptName: Boolean, batchText: String)
    fun setParameterOrder(parameters: List<Parameter>)
    fun deleteParameter(parameter: Parameter)
    fun getValues(parameter: Parameter): List<Pair<String, String>>
    fun putValue(environment: Environment, parameter: Parameter, value: String)
    fun getBackups(): List<Backup>
    fun addBackup(comment: String)
    fun restoreBackup(backup: Backup)
    fun deleteBackup(backup: Backup)

    data class Environment(val id: String) {
        var isManuallyManaged = false
        var data = emptyList<String>()

        override fun toString() = "Env($id, $isManuallyManaged, ${data.map { "'$it'" }})"
    }

    data class Parameter(val id: Long) {
        var name = ""
        var position = 0
        var isScriptName = false
        var batchText: String = ""

        override fun toString() = "Param($id, '$name', $isScriptName)"
    }

    data class Backup(val timepoint: Long) {
        var comment = ""

        override fun toString() = "Backup($timepoint, '$comment')"
    }
}