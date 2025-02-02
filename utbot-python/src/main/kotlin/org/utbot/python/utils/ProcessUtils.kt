package org.utbot.python.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class CmdResult(
    val stdout: String,
    val stderr: String,
    val exitValue: Int,
    val terminatedByTimeout: Boolean = false
)

fun startProcess(command: List<String>): Process = ProcessBuilder(command).start()

fun getResult(process: Process, timeout: Long? = null): CmdResult {
    if (timeout != null) {
        if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            process.destroy()
            return CmdResult("", "", 1, terminatedByTimeout = true)
        }
    }

    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var stdout = ""
    var line: String? = ""
    while (line != null) {
        stdout += "$line\n"
        line = reader.readLine()
    }

    if (timeout == null)
        process.waitFor()

    val stderr = process.errorStream.readBytes().decodeToString().trimIndent()
    return CmdResult(stdout.trimIndent(), stderr, process.exitValue())
}

fun runCommand(command: List<String>, timeout: Long? = null): CmdResult {
    val process = startProcess(command)
    return getResult(process, timeout)
}