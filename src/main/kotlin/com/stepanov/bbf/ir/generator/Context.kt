package com.stepanov.bbf.ir.generator

class Context (private val data: MetaInfo,
               private val classes: MutableSet<GeneralClass> = mutableSetOf(),
               private val methods: MutableSet<GeneralMethod> = mutableSetOf(),
               private val variables: MutableSet<GeneralVariable> = mutableSetOf(),
               private val genMVC: MutableSet<GeneralVMC> = mutableSetOf(),
               private val masterContext: Context? = null,
               private val containTypes: MutableSet<String> = mutableSetOf()) {
    fun generateLayer(depth: Int){
        if(depth > 0){
            for(i in 0..rnd(data["minWidth"], data["maxWidth"])){
                if(rnd(0, 100) < data["genClass_pr"]) generateClass( depth - 1)
                if(rnd(0, 100) < data["genMethod_pr"]) generateMethod( depth - 1)
                if(rnd(0, 100) < data["genVariable_pr"]) generateVariable( depth - 1)
            }
        }
    }
    private fun generateClass(depth: Int){
        val genericTypes = if(rnd(0, 100) < data["genClass_isGeneric"]) null else mutableSetOf<String>().let {
            for (i in 0 .. rnd(data["genClass_GenericMinCount"],data["genClass_GenericMaxCount"])){
                val genName = generateName("gen", depth)
                it.add(genName)
                containTypes.add(genName)
            }
            return@let it
        }
        val cl = GeneralClass(generateName("cl", depth), genericTypes,this)
        cl.context.generateLayer(depth)
        classes.add(cl)
        genMVC.add(cl)
    }
    private fun generateMethod(depth: Int){
        val returnType = getRandomType()
        val method = GeneralMethod(generateName("mt", depth), this, getRandomTypeSet(), returnType)
        method.context.generateLayer(depth)
        methods.add(method)
        genMVC.add(method)
        //containTypes.add(returnType)
    }
    private fun generateVariable( depth: Int){
        val value = GeneralVariable(generateName("vr", depth), getRandomType())
        variables.add(value)
        genMVC.add(value)
    }
    private fun getRandomType(): String = rnd(0, 100).let { rnd ->
        when {
            rnd in 0..29&& classes.isNotEmpty()  -> classes.let { it.toList()[rnd(0, it.size)] }.getName()
            rnd in 30 .. 59 && masterContext != null && masterContext.classes.isNotEmpty() -> masterContext.classes.let { it.toList()[rnd(0, it.size)] }.getName()
            rnd in 60 .. 89 && containTypes.isNotEmpty() -> containTypes.let { it.toList()[rnd(0, it.size)] }
            else -> defaultTypes[rnd(0, 8)]
        }
    }

    private fun getRandomTypeSet(): Set<String>{
        val mutableSet = mutableSetOf<String>()
        for (i in 0..10){
            if(rnd(0, 100) < 50){
                mutableSet.add(getRandomType())
            }
        }
        return mutableSet
    }

    override fun toString(): String = StringBuilder().let{ sb ->
        for(mvc in genMVC){
            sb.append("${createWithLeadingSpaces(mvc.toString())}\n")
        }
        sb.toString()
    }
}