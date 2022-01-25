package backend

import java.util.prefs.Preferences

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Preferences {
    companion object {
        const val PREF_STAGE_WIDTH = "stage_width"
        const val PREF_STAGE_HEIGHT = "stage_height"
        const val PREF_STAGE_X = "stage_x"
        const val PREF_STAGE_Y = "stage_y"
        const val PREF_ENV_TABLE_COLUMN_WIDTHS = "env_table_column_width"
        const val PREF_PARAM_TABLE_COLUMN_WIDTHS = "param_table_column_width"
        const val PREF_BACKUP_TABLE_COLUMN_WIDTHS = "backup_table_column_width"
    }

    private val preferences = Preferences.userNodeForPackage(javaClass)

    fun get(key: String): String? = preferences.get(key, null)
    fun getInt(key: String) = get(key)?.toIntOrNull()
    fun getDouble(key: String) = get(key)?.toDoubleOrNull()

    fun getList(key: String) = get(key)?.split("\n")?.dropLast(1)
    fun getIntList(key: String) = getList(key)?.mapNotNull { it.toIntOrNull() }
    fun getDoubleList(key: String) = getList(key)?.mapNotNull { it.toDoubleOrNull() }

    fun put(key: String, value: Any) = preferences.put(key, "$value")
    fun putList(key: String, value: List<Any>) = put(key, "${value.joinToString("\n")}\n")
}