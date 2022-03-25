package com.stepanov.bbf.ir.generator

import java.io.File

class MetaInfo(val path: String) {
    val data = File(path).readLines().associate {
        val dataLine = it.split("-").map { line -> line.trim() }
        val param = dataLine[0].split(Regex("""\s+"""))
        Pair(param[0], param[1].toInt())
    }

    operator fun get(name: String): Int = data[name] ?: throw IllegalArgumentException(name)
}