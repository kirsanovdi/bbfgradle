package com.stepanov.bbf.ir

import com.stepanov.bbf.ir.generator.Context
import com.stepanov.bbf.ir.generator.MetaInfo

fun main() {
    val emptyContext = Context(MetaInfo("data.txt"))
    emptyContext.generateLayer(4)
    println(emptyContext.toString().replace("    }", "}"))
}