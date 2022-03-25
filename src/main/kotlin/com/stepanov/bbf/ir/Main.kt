package com.stepanov.bbf.ir

import com.stepanov.bbf.ir.generator.Context
import com.stepanov.bbf.ir.generator.MetaInfo
import kotlin.system.measureTimeMillis

fun main() {
    val emptyContext = Context(MetaInfo("data.txt"))
    println(measureTimeMillis {
        emptyContext.generateClassLayer(2)
    })
    println("--------------------------------------")
    println(emptyContext.toString().replace("    }", "}"))
}