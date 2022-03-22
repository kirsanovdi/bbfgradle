package com.stepanov.bbf.ir.generator

class GeneralClass(private val name: String,
                   private val genericTypes: Set<String>?,
                   masterContext: Context): GeneralVMC(){
    val context = getEmptyContextWithMaster(masterContext)
    override fun toString(): String {
        return "class${if(genericTypes != null && genericTypes.isNotEmpty()) "<${genericTypes.joinToString()}>" else ""} $name {\n$context}"
    }
    fun getName(): String = name
}