package com.stepanov.bbf.ir.converter

import com.stepanov.bbf.bugfinder.BugFinder
import com.stepanov.bbf.bugfinder.executor.compilers.KJCompiler
import com.stepanov.bbf.bugfinder.executor.project.Project
import com.stepanov.bbf.bugfinder.util.noBoxFunModifying
import com.stepanov.bbf.bugfinder.util.noLastLambdaInFinallyBlock
import kotlin.system.exitProcess

fun main() {
    val p = Project.createFromCode("""
        fun main(){
            println(123)
        }
    """.trimIndent())
    val bf = BugFinder("tmp/tmp.kt")
    println(p.files.first().psiFile.text)
    println("--------------------------")
    bf.mutate(p, p.files.first(), listOf(KJCompiler()), listOf(::noBoxFunModifying, ::noLastLambdaInFinallyBlock))
    println("--------------------------")
    println(p.files.first().psiFile.text)
    val compiler = KJCompiler()
    val compiled = compiler.compile(p)
    println(compiled)
    println(compiler.exec(compiled.pathToCompiled))
    exitProcess(0)

}