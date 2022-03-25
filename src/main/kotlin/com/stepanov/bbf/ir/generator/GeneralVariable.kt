package com.stepanov.bbf.ir.generator

class GeneralVariable(val name: String, val generalClass: GeneralClass, var state: VariableState) : GeneralVMC() {
    private val firstState = state
    var init: GeneralInit? = null
    override fun toString(): String {
        return "$firstState variable $name [${generalClass.getName()}]${if (init != null) " = ${init!!.getInitLine()}" else ""}"
    }
}