package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.ShowHelpException
import com.xenomachina.argparser.SystemExitException
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> withArgs(args: Array<String>, block: T.() -> Unit) {
    val argsInstance = ArgParser(args).parseInto(T::class.primaryConstructor as (ArgParser) -> T)
    block.invoke(argsInstance)
}

inline fun <reified T : Any> withArgs(args: List<String>, block: T.() -> Unit) {
    withArgs(args.toTypedArray(), block)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> renderHelp(clazz: KClass<T>, commandName: String): String {
    try {
        ArgParser(arrayOf("-h")).parseInto(clazz.primaryConstructor as (ArgParser) -> T)
    } catch (e: ShowHelpException) {
        val bos = ByteArrayOutputStream()
        val writer = OutputStreamWriter(bos, StandardCharsets.UTF_8)
        e.printUserMessage(writer, commandName, 80)
        writer.flush()
        bos.flush()
        return bos.toString("utf-8")
    }
    throw IllegalStateException("cannot render help for $commandName for args class ${clazz.qualifiedName}")
}

/**
 * Command line tool to interact with stellar. Use this at your own risk.
 */
fun main(args: Array<String>) {
    try {
        val defaultArgs = System.getenv("CLISTE_ARGS")
        val joinedArgs: Array<String>
        if(StringUtils.isNotBlank(defaultArgs)) {
            joinedArgs = defaultArgs?.trim()?.split(" ")?.toTypedArray()?.plus(args) ?: args
        } else {
            joinedArgs =  args
        }
        val cliSteArgs = ArgParser(joinedArgs).parseInto(::CliSteArgs)
        if (cliSteArgs.verbose) {
            println("""CLISTE_ARGS = ${defaultArgs ?: ""}
                |commandline args: ${joinedArgs.joinToString(" ")}
            """.trimMargin())
        }
        CommandContext(cliSteArgs).run()
    } catch (e: SystemExitException) {
        if (e is ShowHelpException) {
            println(
                """
            |Cliste Introduction
            |
            |CliSte is a simple command line tool to interact with stellar.
            |
            |Disclaimer: It is assumed that you know what you are doing and if you choose to
            |use this with the public chain, you do so at your own risk.
            |
            |""".trimMargin()
            )
        }
        e.printAndExit("cliste")
    }
}
