package com.stepanov.bbf.ir.generator

class Context(
    private val data: MetaInfo,
    private val classes: MutableSet<GeneralClass> = mutableSetOf(),
    private val methods: MutableSet<GeneralMethod> = mutableSetOf(),
    private val variables: MutableSet<GeneralVariable> = mutableSetOf(),
    private val genMVC: MutableSet<GeneralVMC> = mutableSetOf(),
    private val masterContext: Context? = null,
) {

    fun generateLayer(depth: Int) {
        if (depth > 0) {
            for (i in 0 until rnd(data["minWidth"], data["maxWidth"])) {
                for (j in 0 until rnd(0, 4)) {
                    if (rnd(0, 100) < data["genClass_pr"]) generateClass(depth - 1)
                    if (rnd(0, 100) < data["genMethod_pr"]) generateMethod(depth - 1)
                    if (rnd(0, 100) < data["genVariable_pr"]) generateVariable(depth - 1, VariableState.NONINIT)
                    if (rnd(0, 100) < data["genInit_pr"]) generateRandomInit()
                }
            }
        }
    }

    fun generateClassLayer(depth: Int) {
        if (depth > 0) {
            for (i in 0 until rnd(data["minWidth"], data["maxWidth"])) {
                if (rnd(0, 100) < data["genVariable_pr"]) generateVariable(depth - 1, VariableState.CLASSVAL)
            }
            for (i in 0 until rnd(data["minWidth"], data["maxWidth"])) {
                if (rnd(0, 100) < data["genClass_pr"]) generateClass(depth - 1)
                if (rnd(0, 100) < data["genMethod_pr"]) generateMethod(depth - 1)
            }
        }
        for (variable in variables) variable.state = VariableState.CLASSVAL
    }

    private fun generateClass(depth: Int) {
        val genericClasses = if (rnd(0, 100) < data["genClass_isGeneric"]) null else mutableSetOf<String>().let {
            for (i in 0..rnd(data["genClass_GenericMinCount"], data["genClass_GenericMaxCount"])) {
                val genName = generateName("gen", depth)
                it.add(genName)
            }
            return@let it
        }
        val cl = GeneralClass(generateName("cl", depth), null/*genericClasses*/, this)
        cl.context.generateClassLayer(depth)
        classes.add(cl)
        genMVC.add(cl)
    }

    private fun generateMethod(depth: Int) {
        val returnClass = getRandomClass()
        val method = GeneralMethod(
            generateName("mt", depth),
            this,
            getRandomClassList(rnd(data["minRndClassListSize"], data["maxRndClassListSize"])),
            returnClass
        )
        method.context.generateLayer(depth)
        methods.add(method)
        genMVC.add(method)
        //containTypes.add(returnType)
    }

    private fun generateVariable(depth: Int, state: VariableState) {
        val value = if (rnd(0, 100) < data["createAtInit_pr"] && state == VariableState.NONINIT) {
            val prValue = GeneralVariable(generateName("vr", depth), getRandomClass(), VariableState.INIT)
            prValue.init = generateInit(prValue)
            prValue
        } else GeneralVariable(generateName("vr", depth), getRandomClass(), state)
        variables.add(value)
        genMVC.add(value)
    }

    private fun getRandomLevelClass(): GeneralClass = rnd(0, 100).let { rnd ->
        when {
            rnd in 0..100 && classes.isNotEmpty() -> classes.elementAt(rnd(0, classes.size))
            else -> defaultTypes[rnd(0, 8)]
        }
    }

    private fun getRandomClass(): GeneralClass = when (rnd(0, 100)) {
        in 0..49 -> getRandomLevelClass()
        in 50..99 -> hookUpperClass(data["hookUpperClass"])
        else -> defaultTypes[rnd(0, 8)]
    }


    private fun getRandomClassList(count: Int): List<GeneralClass> {
        val mutableList = mutableListOf<GeneralClass>()
        for (i in 0 until count) {
            mutableList.add(getRandomClass())
        }
        return mutableList
    }

    private fun generateRandomInit() {
        if (variables.isNotEmpty()) {
            val generalInit = generateInit(variables.elementAt(rnd(0, variables.size)))
            genMVC.add(generalInit)
        } else println("no variable for set")
    }

    private fun generateInit(generalVariable: GeneralVariable): GeneralInit {
        val otherVariable = getRandomInitVariableByClass(generalVariable)
        println(if (otherVariable != null) 1 else 0)
        val generalInit = if (rnd(0, 100) < data["createNewObject_pr"] || otherVariable == null) {
            GeneralInit(
                generalVariable, "new ${generalVariable.generalClass.getName()}(${
                    generalVariable.generalClass.let { currentClass ->
                        //val generic = currentClass.genericClasses
                        val params = currentClass.context.variables.map { generateInit(it) }
                        params.joinToString { it.getInitLine() }
                    }
                })"
            )
        } else {
            GeneralInit(generalVariable, otherVariable.name)
        }
        generalVariable.state = VariableState.INIT
        return generalInit
    }

    private fun getRandomInitVariableLevelByClass(generalVariable: GeneralVariable): GeneralVariable? =
        variables.filter { it.generalClass == generalVariable.generalClass && it.state == VariableState.INIT && it != generalVariable }
            .let { if (it.isEmpty()) null else it[rnd(0, it.size)] }

    private fun getRandomInitVariableByClass(generalVariable: GeneralVariable): GeneralVariable? =
        getRandomInitVariableLevelByClass(generalVariable).let {
            if (it == null && masterContext != null) masterContext.getRandomInitVariableByClass(generalVariable) else it
        }

    private fun hookUpperClass(probability: Int): GeneralClass =
        if (masterContext != null && rnd(0, 100) < probability) hookUpperClass(probability) else getRandomLevelClass()

    override fun toString(): String = StringBuilder().let { sb ->
        for (mvc in genMVC) {
            sb.append("${createWithLeadingSpaces(mvc.toString())}\n")
        }
        sb.toString()
    }
}