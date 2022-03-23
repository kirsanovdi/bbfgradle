package com.stepanov.bbf.ir.generator

class Context (private val data: MetaInfo,
               private val classes: MutableList<GeneralClass> = mutableListOf(),
               private val methods: MutableList<GeneralMethod> = mutableListOf(),
               private val variables: MutableList<GeneralVariable> = mutableListOf(),
               private val genMVC: MutableList<GeneralVMC> = mutableListOf(),
               private val masterContext: Context? = null,
               private val containTypes: MutableList<String> = mutableListOf()) {
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
        val genericTypes = if(rnd(0, 100) < data["genClass_isGeneric"]) null else mutableListOf<String>().let {
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
        val method = GeneralMethod(generateName("mt", depth), this, getRandomTypeList(rnd(data["minRndTypeListSize"], data["maxRndTypeListSize"])), returnType)
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

    private fun getRandomLevelType(): String = rnd(0, 100).let { rnd ->
        when {
            rnd in 0..49&& classes.isNotEmpty()  -> classes[rnd(0, classes.size)].getName()
            rnd in 50 .. 99 && containTypes.isNotEmpty() -> containTypes[rnd(0, containTypes.size)]
            else -> defaultTypes[rnd(0,8)]
        }
    }

    private fun getRandomType(): String = when (rnd(0, 100)) {
        in 0 .. 49 -> getRandomLevelType()
        in 50 .. 99 -> hookUpperType(data["hookUpperType"])
        else -> defaultTypes[rnd(0, 8)]
    }


    private fun getRandomTypeList(count: Int): List<String>{
        val mutableList = mutableListOf<String>()
        for (i in 0 until count){
            mutableList.add(getRandomType())
        }
        return mutableList
    }

    private fun hookUpperType(probability: Int): String =
        if(masterContext != null && rnd(0, 100) < probability) hookUpperType(probability) else getRandomLevelType()

    override fun toString(): String = StringBuilder().let{ sb ->
        for(mvc in genMVC){
            sb.append("${createWithLeadingSpaces(mvc.toString())}\n")
        }
        sb.toString()
    }
}