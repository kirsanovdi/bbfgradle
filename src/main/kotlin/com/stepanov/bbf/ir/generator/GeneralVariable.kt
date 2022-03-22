package com.stepanov.bbf.ir.generator

class GeneralVariable(val name: String, val type: String): GeneralVMC() {
    override fun toString(): String {
        return "variable $name $type"
    }
}