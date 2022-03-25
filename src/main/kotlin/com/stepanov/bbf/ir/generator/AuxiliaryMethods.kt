package com.stepanov.bbf.ir.generator

import kotlin.random.Random

fun getEmptyContext(): Context = Context(MetaInfo("data.txt"))
fun getEmptyContextWithMaster(masterContext: Context): Context =
    Context(MetaInfo("data.txt"), masterContext = masterContext)

fun createWithLeadingSpaces(string: String): String = string.replace("\n", "\n    ")

val contextForDefaultTypes = getEmptyContext()
val defaultTypes = listOf(
    GeneralClass("Int", null, contextForDefaultTypes),
    GeneralClass("Double", null, contextForDefaultTypes),
    GeneralClass("Float", null, contextForDefaultTypes),
    GeneralClass("Long", null, contextForDefaultTypes),
    GeneralClass("Byte", null, contextForDefaultTypes),
    GeneralClass("Short", null, contextForDefaultTypes),
    GeneralClass("Char", null, contextForDefaultTypes),
    GeneralClass("Boolean", null, contextForDefaultTypes)
)

fun rnd(from: Int, to: Int): Int {
    return if (from == to) from else Random.nextInt(to - from) + from
}

var i = 0
fun generateName(prefix: String, depth: Int): String {
    return "${prefix}Rd${depth}Id${i++}"
}

abstract class GeneralVMC

enum class VariableState {
    NONINIT, INIT, CLASSVAL, FINAL
}