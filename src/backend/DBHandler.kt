package backend

import backend.DBHandlerInterface.*
import org.sqlite.SQLiteException
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

@Suppress("unused")
class DBHandler : DBHandlerInterface {
    companion object {
        private val DB_URL = "jdbc:sqlite:${System.getProperty("user.dir")}\\StartScriptCreator.db"
        private val BACKUP_TABLES = listOf("info", "env", "param", "value")
    }

    private val conn: Connection = DriverManager.getConnection(DB_URL)

    init {
        //language=SQL
        val init = """
            | PRAGMA foreign_keys = ON;
            | CREATE TABLE IF NOT EXISTS info (
            |    key TEXT PRIMARY KEY,
            |    value TEXT
            | );
            | CREATE TABLE IF NOT EXISTS env (
            |    id TEXT PRIMARY KEY,
            |    data TEXT NOT NULL DEFAULT ''
            | );
            | CREATE TABLE IF NOT EXISTS param (
            |    id INTEGER PRIMARY KEY,
            |    name TEXT NOT NULL UNIQUE
            | );
            | CREATE TABLE IF NOT EXISTS value (
            |    env TEXT,
            |    param INT,
            |    value TEXT NOT NULL,
            |    PRIMARY KEY (env, param),
            |    FOREIGN KEY (env) REFERENCES env(id) ON DELETE CASCADE,
            |    FOREIGN KEY (param) REFERENCES param(id) ON DELETE CASCADE
            | );
            | CREATE TABLE IF NOT EXISTS backup (
            |    timepoint INT PRIMARY KEY,
            |    comment TEXT
            | );
            """.trimMargin()

        conn.createStatement().useAs {
            for (stmt in init.split(";")) {
                if (stmt.isNotBlank()) {
                    execute(stmt)
                }
            }
        }

        val dbVersion = getInfo("dbVersion").toIntOrNull() ?: 0
        if (dbVersion < 4) {
            println("DBVersion: $dbVersion")
            putInfo("dbVersion", "4")
            val timepoints = getBackups().map { it.timepoint.toString() }.toMutableList().apply { add("") }
            fun addColumn(table: String, column: String) {
                println("Add column: $table, $column")
                try {
                    conn.createStatement().useAs {
                        for (timepoint in timepoints) {
                            execute("ALTER TABLE $table$timepoint ADD $column")
                        }
                    }
                }catch (e: Exception) {
                    throw e
                }
            }
            if (dbVersion < 2) addColumn("env", "isManuallyManaged INT DEFAULT 0")
            if (dbVersion < 1) addColumn("param", "position INT DEFAULT 0")
            addColumn("param", "isScriptName INT")
            addColumn("param", "batchText TEXT DEFAULT ''")
            getParameters().find { it.name == "Script Name" }?.let {
                updateParameter(it, it.name, true, getInfo("batch"))
            }
        }
    }

    override fun getInfo(key: String): String {
        return conn.prepareStatement("SELECT value FROM info WHERE key=?").useAs {
            setString(1, key)
            executeQuery().useAs {
                if (next()) getString("value") else ""
            }
        }
    }

    override fun putInfo(key: String, value: Any?) {
        conn.prepareStatement("INSERT OR REPLACE INTO info (key, value) VALUES (?, ?)").useAs {
            setString(1, key)
            setString(2, "$value")
            execute()
        }
    }

    override fun getEnvironments(): List<Environment> {
        conn.createStatement().useAs {
            executeQuery("SELECT * FROM env ORDER BY id").useAs {
                val envs = ArrayList<Environment>()
                while (next()) {
                    envs.add(
                        Environment(getString("id")).apply {
                            data = getString("data").split("\n")
                            isManuallyManaged = getBoolean("isManuallyManaged")
                        }
                    )
                }
                return envs
            }
        }
    }

    override fun addEnvironment(id: String, data: List<String>, manually: Boolean) {
        conn.prepareStatement("INSERT INTO env (id, data, isManuallyManaged) VALUES (?, ?, ?)").useAs {
            setString(1, id)
            setString(2, data.joinToString("\n"))
            setBoolean(3, manually)
            execute()
        }
    }

    override fun updateEnvironment(id: String, data: List<String>, manually: Boolean) {
        conn.prepareStatement("UPDATE env SET data=?, isManuallyManaged=? WHERE id=?").useAs {
            setString(1, data.joinToString("\n"))
            setBoolean(2, manually)
            setString(3, id)
            execute()
        }
    }

    override fun deleteEnvironment(environment: Environment) {
        conn.prepareStatement("DELETE FROM env WHERE id=?").useAs {
            setString(1, environment.id)
            execute()
        }
    }

    override fun getParameters(): List<Parameter> {
        conn.createStatement().useAs {
            executeQuery("SELECT * FROM param ORDER BY position, name").useAs {
                val params = ArrayList<Parameter>()
                while (next()) {
                    params.add(
                        Parameter(getLong("id")).apply {
                            name = getString("name")
                            position = getInt("position")
                            isScriptName = getBoolean("isScriptName")
                            batchText = getString("batchText")
                        }
                    )
                }
                return params
            }
        }
    }

    override fun addParameter(name: String, isScriptName: Boolean) {
        val intScriptName = if (isScriptName) 1 else 0
        conn.prepareStatement(
            "INSERT OR IGNORE INTO param (name, isScriptName, position) VALUES (?, $intScriptName, 0)"
        ).useAs {
            setString(1, name)
            execute()
        }
    }

    override fun updateParameter(parameter: Parameter, name: String, isScriptName: Boolean, batchText: String) {
        val intScriptName = if (isScriptName) 1 else 0
        conn.prepareStatement(
            "UPDATE param SET name=?, isScriptName=$intScriptName, batchText=? WHERE id=${parameter.id}"
        ).useAs {
            setString(1, name)
            setString(2, batchText)
            execute()
        }
    }

    override fun setParameterOrder(parameters: List<Parameter>) {
        val cases = parameters.joinToString(" ") { "WHEN ${it.id} THEN ${it.position}" }
        val ids = parameters.map { it.id }.joinToString()
        val bulkUpdate = "UPDATE param SET position = CASE id $cases END WHERE id IN ($ids)"
        conn.createStatement().useAs {
            execute(bulkUpdate)
        }
    }

    override fun deleteParameter(parameter: Parameter) {
        conn.createStatement().useAs {
            execute("DELETE FROM param WHERE id = ${parameter.id}")
        }
    }

    override fun getValues(parameter: Parameter): List<Pair<String, String>> {
        conn.createStatement().useAs {
            executeQuery("SELECT env, value FROM value WHERE param=${parameter.id} ORDER BY value").useAs {
                val list = ArrayList<Pair<String, String>>()
                while (next()) {
                    list.add(Pair(getString("env"), getString("value")))
                }
                return list
            }
        }
    }

    override fun putValue(environment: Environment, parameter: Parameter, value: String) {
        val sql = "INSERT OR REPLACE INTO value (env, param, value) VALUES (?, ${parameter.id}, ?)"
        conn.prepareStatement(sql).useAs {
            setString(1, environment.id)
            setString(2, value)
            execute()
        }
    }

    override fun getBackups(): List<Backup> {
        conn.createStatement().useAs {
            executeQuery("SELECT * FROM backup ORDER BY timepoint DESC").useAs {
                val backups = ArrayList<Backup>()
                while (next()) {
                    backups.add(Backup(getLong("timepoint")).apply {
                        comment = getString("comment")
                    })
                }
                return backups
            }
        }
    }

    override fun addBackup(comment: String) {
        val timepoint = System.currentTimeMillis()
        val sql = "INSERT INTO backup (timepoint, comment) VALUES ($timepoint, ?)"
        conn.prepareStatement(sql).useAs {
            setString(1, comment)
            execute()
        }
        conn.createStatement().useAs {
            for (table in BACKUP_TABLES) {
                execute("CREATE TABLE $table$timepoint AS SELECT * FROM $table")
            }
        }
    }

    override fun restoreBackup(backup: Backup) {
        conn.createStatement().useAs {
            for (table in BACKUP_TABLES) {
                execute("DELETE FROM $table")
                execute("INSERT INTO $table SELECT * FROM $table${backup.timepoint}")
            }
        }
    }

    override fun deleteBackup(backup: Backup) {
        conn.createStatement().useAs {
            execute("DELETE FROM backup WHERE timepoint = ${backup.timepoint}")
            for (table in BACKUP_TABLES) {
                execute("DROP TABLE IF EXISTS $table${backup.timepoint}")
            }
        }
    }

    override fun close() = conn.close()

}
