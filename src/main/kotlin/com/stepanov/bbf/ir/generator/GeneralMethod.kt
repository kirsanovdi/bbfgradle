package com.stepanov.bbf.ir.generator

class GeneralMethod(
    private val name: String,
    masterContext: Context,
    private val params: List<GeneralClass>,
    private val returnClassType: GeneralClass
) : GeneralVMC() {
    val context = getEmptyContextWithMaster(masterContext)
    override fun toString(): String {
        return "method $name [${params.joinToString { it.getName() }}] -> [${returnClassType.getName()}] {\n$context}"
    }

    fun getName(): String = name
}