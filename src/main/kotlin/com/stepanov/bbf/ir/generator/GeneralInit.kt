package com.stepanov.bbf.ir.generator

class GeneralInit(
    private val variable: GeneralVariable,
    private val initializerLine: String
) : GeneralVMC() {
    override fun toString(): String {
        return "${variable.name} = $initializerLine"
    }

    fun getInitLine(): String = initializerLine

}