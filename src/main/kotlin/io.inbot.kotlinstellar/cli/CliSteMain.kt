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
        e.printUserMessage(writer, commandName, 160)
        writer.flush()
        bos.flush()
        return bos.toString("utf-8").replace("positional arguments:", "### Positional arguments\n").replace("optional arguments:", "### Optional arguments\n")
    }
    throw IllegalStateException("cannot render help for $commandName for args class ${clazz.qualifiedName}")
}

fun findCommandPos(args: Array<String>): Int {
    var counter = 0
    while (counter < args.size &&
        try {
            Commands.valueOf(args[counter])
        } catch (e: IllegalArgumentException) {
            null
        } == null
    ) {
        counter++
    }
    return counter
}

fun splitOnCommand(args: Array<String>): Pair<Array<String>, Array<String>> {
    val pos = findCommandPos(args)
    return if (pos <args.size - 1) {
        Pair(args.copyOfRange(0, pos + 1), args.copyOfRange(pos + 1, args.size))
    } else {
        Pair(args, arrayOf())
    }
}

/**
 * Command line tool to interact with stellar. Use this at your own risk.
 */
fun main(args: Array<String>) {
    try {
        val defaultArgs = System.getenv("CLISTE_ARGS")
        val joinedArgs: Array<String>
        joinedArgs = if (StringUtils.isNotBlank(defaultArgs)) {
            defaultArgs?.trim()?.split(" ")?.toTypedArray()?.plus(args) ?: args
        } else {
            args
        }
        if (joinedArgs.isEmpty()) {
            throw SystemExitException("missing options; run cliste help", 1)
        }
        // split so we can pass the command args to the command specific parser without having the main args parser break
        val (mainArgs, commandArgs) = splitOnCommand(joinedArgs)

        val cliSteArgs = ArgParser(mainArgs).parseInto(::CliSteArgs)
        if (cliSteArgs.verbose) {
            println(
                """CLISTE_ARGS = ${defaultArgs ?: ""}
                |commandline args: ${joinedArgs.joinToString(" ")}
            """.trimMargin()
            )
        }

        CommandContext(cliSteArgs, commandArgs).use {
            it.run()
        }
    } catch (e: SystemExitException) {
        if (e is ShowHelpException) {
            println(
                """
            |Cliste
            |
            |CliSte is a simple command line tool to interact with stellar.
            |
            |Disclaimer: It is assumed that you know what you are doing and if you choose to
            |use this with the public chain, you do so at your own risk.
            |
            |""".trimMargin()
            )
        }
//        e.printStackTrace()
        e.printAndExit("cliste")
    }
}
