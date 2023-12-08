package com.example

import com.arangodb.ArangoDB
import com.arangodb.ArangoDatabase
import com.arangodb.Protocol
import com.arangodb.entity.LoadBalancingStrategy
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException

fun main(args: Array<String>) {
    try {
        val dbConfig = DatabaseConfig().initFromArgs(args)
        prepareOutputDir(dbConfig.outputDir)
        val connection = createDbConnection(dbConfig)
        val db = connection.db(dbConfig.database)
        exportDb(db, dbConfig.outputDir)
        connection.shutdown()
    } catch (e: Exception) {
        println("Program exit with error:")
        e.printStackTrace()
    }
}

fun exportDb(db: ArangoDatabase, outputDir: String) {
    val objectMapper = ObjectMapper()
    db.collections
        .filter { !it.name.startsWith("_") }
        .map {
            val query = "FOR x IN ${it.name} RETURN x"
            val cursor = db.query(query, Map::class.java)
            val documents = mutableListOf<Map<*, *>>()
            while (cursor.hasNext()) {
                documents.add(cursor.next())
            }
            cursor.close()
            try {
                val filename = "${outputDir}${File.separator}${it.name}.json"
                println("Write document ${filename}.json")
                objectMapper.writeValue(File(filename), documents)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
}

fun prepareOutputDir(dir: String) {
    val directory = File(dir)
    if (!directory.exists() && directory.mkdir()) {
        println("Successfully create directory [$dir]")
    } else {
        println("Directory [$dir] already exists")
    }
}

fun getPasswordInputFromConsole(): String? {
    val console = System.console()
    return if (console != null)
        String(console.readPassword("Please enter db password: "))
    else null
}

const val SUPER_SECURE_PASSWORD = "password"

data class DatabaseConfig(
    var host: String = "",
    var port: Int = 0,
    var username: String = "",
    var password: String = "",
    var database: String = "",
    var outputDir: String = "",
    var maxConnection: Int = 10
) {
    fun initFromArgs(args: Array<String>): DatabaseConfig {
        args.map {
            val itSplit = it.split("=")
            val key = itSplit[0]
            val value = itSplit[1]
            when (key) {
                "--server.endpoint" -> {
                    val x = value.split(":")
                    this.host = x[0]
                    this.port = x[1].toInt()
                }

                "--server.username" -> this.username = value
                "--server.database" -> this.database = value
                "--output-directory" -> this.outputDir = value
                else -> {}
            }
        }
        this.password = getPasswordInputFromConsole() ?: SUPER_SECURE_PASSWORD
        return this
    }

    override fun toString(): String {
        return """
            DatabaseConfig(
                host = $host,
                port = $port,
                username = $username,
                password = ${"*".repeat(password.length)},
                database = $database,
                outputDir = $outputDir,
                maxConnection = $maxConnection,
            )
        """.trimIndent()
    }
}

fun createDbConnection(databaseConfig: DatabaseConfig): ArangoDB {
    return ArangoDB.Builder()
        .host(databaseConfig.host, databaseConfig.port)
        .user(databaseConfig.username)
        .password(databaseConfig.password)
        .maxConnections(databaseConfig.maxConnection) // pooling
        .protocol(Protocol.HTTP_JSON)
        .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
        .build()
}
