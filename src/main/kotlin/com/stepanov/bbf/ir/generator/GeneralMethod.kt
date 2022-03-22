package com.stepanov.bbf.ir.generator

class GeneralMethod(private val name: String,
                    masterContext: Context,
                    private val paramsTypes: Set<String>,
                    private val returnType: String): GeneralVMC(){
    val context = getEmptyContextWithMaster(masterContext)
    override fun toString(): String {
        return "method $name (${paramsTypes.joinToString()}) -> $returnType {\n$context}"
    }
    fun getName(): String = name
}