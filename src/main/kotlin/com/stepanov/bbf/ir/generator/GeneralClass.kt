package com.stepanov.bbf.ir.generator

class GeneralClass(
    private val name: String,
    val genericClasses: List<String>?,
    masterContext: Context
) : GeneralVMC() {
    val context = getEmptyContextWithMaster(masterContext)
    override fun toString(): String {
        return "class${if (genericClasses != null && genericClasses.isNotEmpty()) "<${genericClasses.joinToString()}>" else ""} $name {\n$context}"
    }

    fun getName(): String = name
}