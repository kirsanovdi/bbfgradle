package com.stepanov.bbf.ir.generator

import kotlin.random.Random

//fun getEmptyContext(): Context = Context(MetaInfo("data.txt"))
fun getEmptyContextWithMaster(masterContext: Context): Context = Context(MetaInfo("data.txt"), masterContext = masterContext)
fun createWithLeadingSpaces(string: String): String = string.replace("\n", "\n    ")
val defaultTypes = listOf("Int", "Double", "Float", "Long", "Byte", "Short", "Char", "Boolean")
fun rnd(from: Int, to: Int): Int{
    return if (from == to) from else Random.nextInt(to - from) + from
}

var i = 0
fun generateName(prefix: String, depth: Int): String{
    return "$prefix${ (depth + 1) * 1000 + i++}"
}
abstract class GeneralVMC