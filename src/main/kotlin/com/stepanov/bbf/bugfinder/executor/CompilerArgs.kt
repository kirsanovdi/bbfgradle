package com.stepanov.bbf.bugfinder.executor

import com.stepanov.bbf.bugfinder.executor.compilers.JSCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.util.BBFProperties
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.*
import java.util.jar.JarFile

object CompilerArgs {

    private val file: File = File("bbf.conf")

    fun getPropValue(name: String): String? {
        val props = Properties()
        props.load(file.inputStream())
        return props.getProperty(name)
    }

    fun getPropValueWithoutQuotes(name: String): String {
        val props = Properties()
        props.load(file.inputStream())
        val prop = props.getProperty(name) ?: throw IllegalArgumentException("Cannot init $name property")
        val res = prop.drop(1).dropLast(1)
        return res
    }

    fun getPropAsBoolean(name: String): Boolean = getPropValue(name)?.toBoolean()
        ?: throw IllegalArgumentException("Cannot init $name property")

    fun getStdLibPath(libToSearch: String): String {
        val kotlinVersion = File("build.gradle")
            .readText()
            .lines()
            .firstOrNull { it.trim().contains("kotlin_version") }
            ?.substringAfter('\'')
            ?.substringBefore('\'')
            ?: throw Exception("Dont see kotlinVersion parameter in build.gradle file")
        "tmp/lib/$libToSearch-$kotlinVersion.jar".let {
            require(File(it).exists())
            return it
        }
    }
//    fun getStdLibPath(libToSearch: String): String {
//        val kotlinVersion =
//            File("build.gradle").readText().lines().firstOrNull { it.trim().contains("kotlin_version") }
//                ?: throw Exception("Dont see kotlinVersion parameter in build.gradle file")
//        var ver = kotlinVersion.split("=").last().trim().filter { it != '\'' }
//        val gradleDir = "${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/"
//        var dir =
//            File("$gradleDir/$libToSearch").listFiles()?.find { it.isDirectory && it.name.trim() == ver }?.path ?: ""
//        //TODO fix this
//        if (dir.trim().isEmpty()) {
//            ver = (ver.last() - '0').let { ver.dropLast(1) + (it - 1) }
//            dir = File("$gradleDir/$libToSearch").listFiles()?.find { it.isDirectory && it.name.trim() == ver }?.path
//                ?: ""
//        }
//        if (dir.trim().isEmpty()) {
//            ver = (ver.last() - '0').let { ver.dropLast(1) + (it + 1) }
//            dir = File("$gradleDir/$libToSearch").listFiles()?.find { it.isDirectory && it.name.trim() == ver }?.path
//                ?: ""
//        }
//        var pathToLib = File(dir).walkTopDown().find { it.name == "$libToSearch-$ver.jar" }?.absolutePath ?: ""
//        if (pathToLib.isEmpty()) {
//            ver = "1.5.255-SNAPSHOT"
//            dir = File("$gradleDir/$libToSearch").listFiles()?.find { it.isDirectory && it.name.trim() == ver }?.path
//                ?: ""
//            pathToLib = File(dir).walkTopDown().find { it.name == "$libToSearch-$ver.jar" }?.absolutePath ?: ""
//        }
//        require(pathToLib.isNotEmpty())
//        return pathToLib
//    }

    fun getAnnoPath(ver: String): String {
//        val gradleDir = "${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/org.jetbrains/"
//        val dir =
//            File("$gradleDir/annotations").listFiles()?.find { it.isDirectory && it.name.trim() == ver }?.path ?: ""
//        val pathToLib = File(dir).walkTopDown().find { it.name == "annotations-$ver.jar" }?.absolutePath ?: ""
//        require(pathToLib.isNotEmpty())
        File("tmp/lib/annotations-$ver.jar").let {
            require(it.exists())
            return it.absolutePath
        }
    }

    private fun findAndSaveLib(name: String, jarFile: JarFile) {
        val lib = jarFile.entries().asSequence().first { it.name == name }
        val input = jarFile.getInputStream(lib)
        val content = IOUtils.toString(input, "UTF-8")
        File("tmp/lib/").mkdirs()
        File("tmp/lib/$name").writeText(content)
    }

    fun createJSLib(): String {
        val pathToJar = getStdLibPath("kotlin-stdlib-js")
        val jarFile = JarFile(File(pathToJar))
        findAndSaveLib("kotlin.js", jarFile)
        findAndSaveLib("kotlin.meta.js", jarFile)
        return "${System.getProperty("user.dir")}/tmp/lib/"
    }

    fun getCompilersList(): List<CommonCompiler> {
        val compilers = mutableListOf<CommonCompiler>()
        //Init compilers
        val compilersConf = BBFProperties.getStringGroupWithoutQuotes("BACKENDS")
        compilersConf.filter { it.key.contains("JVM") }.forEach {
            compilers.add(
                JVMCompiler(
                    it.value
                )
            )
        }
        compilersConf.filter { it.key.contains("JS") }.forEach {
            compilers.add(
                JSCompiler(
                    it.value
                )
            )
        }
        return compilers
    }

    val baseDir = getPropValueWithoutQuotes("MUTATING_DIR")
    val javaBaseDir = getPropValueWithoutQuotes("JAVA_FILES_DIR")
    val dirForNewTests = "$baseDir/newTests"

    //PATHS TO COMPILERS
    val pathToJsKotlinLib = createJSLib()
    val pathToTmpFile = getPropValueWithoutQuotes("TMPFILE")
    val pathToTmpDir = pathToTmpFile.substringBeforeLast("/")

    //RESULT
    val resultsDir = getPropValueWithoutQuotes("RESULTS")

    //MODE
    var isMiscompilationMode = getPropAsBoolean("MISCOMPILATION_MODE")
    val isStrictMode = getPropAsBoolean("STRICT_MODE")
    val isPerformanceMode = getPropAsBoolean("PERFORMANCE_MODE")

    //Instrumentation
    var isInstrumentationMode = getPropAsBoolean("WITH_INSTRUMENTATION")
    val isGuidedByCoverage = getPropAsBoolean("GUIDE_FUZZER_BY_COVERAGE")

    //ABI
    val isABICheckMode = getPropAsBoolean("ABI_CHECK_MODE")
    val ignoreMissingClosureConvertedMethod = getPropAsBoolean("IGNORE_MISSING_CLOSURE_CONVERTED_METHOD")

    //ORACLE
    val useJavaAsOracle = getPropAsBoolean("USE_JAVA_AS_ORACLE")

    //COMPILER
    val compilerVersion = File("build.gradle")
        .readText()
        .lines()
        .firstOrNull { it.trim().contains("kotlin_version") }
        ?.substringAfter('\'')
        ?.substringBefore('\'')

    //MUTATED BUGS
    val shouldSaveCompilerBugs =
        getPropAsBoolean("SAVE_BACKEND_EXCEPTIONS")
    val shouldReduceCompilerBugs =
        getPropAsBoolean("REDUCE_BACKEND_EXCEPTIONS")
    val shouldSaveMutatedFiles = getPropAsBoolean("SAVE_MUTATED_FILES")
    val shouldSaveCompileDiff = getPropAsBoolean("SAVE_COMPILER_DIFFS")
    val shouldReduceDiffBehavior =
        getPropAsBoolean("REDUCE_DIFF_BEHAVIOR")
    val shouldReduceDiffCompile =
        getPropAsBoolean("REDUCE_DIFF_COMPILE")

    //REDUKTOR
    val shouldFilterDuplicateCompilerBugs =
        getPropAsBoolean("FILTER_DUPLICATES")

    //JAVA
    val jdkHome = System.getenv("JAVA_HOME")
    val jvmTarget = "1.8"
    val classpath = ""

    //STDLIB
    val jvmStdLibPaths = listOf(
        getStdLibPath("kotlin-stdlib"),
        getStdLibPath("kotlin-stdlib-common"),
        getStdLibPath("kotlin-test"),
        getStdLibPath("kotlin-test-common"),
        getStdLibPath("kotlin-reflect"),
        getStdLibPath("kotlin-script-runtime"),
        getStdLibPath("kotlin-stdlib-jdk8"),
        getStdLibPath("kotlin-stdlib-jdk7")
    )

    val jsStdLibPaths = listOf(
        getStdLibPath("kotlin-stdlib-js"),
        getStdLibPath("kotlin-test-js")
    )
    val pathToStdLibScheme = "tmp/lib/standardLibraryTree.txt"
    val pathToSerializedCommits = "tmp/serializedPatches/"
}