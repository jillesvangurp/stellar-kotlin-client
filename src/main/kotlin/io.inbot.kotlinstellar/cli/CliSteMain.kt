package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.ShowHelpException
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
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
inline fun <reified T : Any> renderHelp(commandName: String): String {
    try {
        ArgParser(arrayOf("-h")).parseInto(T::class.primaryConstructor as (ArgParser) -> T)
    } catch (e: ShowHelpException) {
        val bos = ByteArrayOutputStream()
        val writer = OutputStreamWriter(bos, StandardCharsets.UTF_8)
        e.printUserMessage(writer, commandName, 0)
        writer.flush()
        bos.flush()
        return bos.toString("utf-8")
    }

    return "should not happen";
}

/**
 * Command line tool to interact with stellar. Use this at your own risk.
 */
fun main(args: Array<String>) {
    try {
        val cliSteArgs = ArgParser(args).parseInto(::CliSteArgs)
        CommandContext(cliSteArgs).run()
    } catch (e: ShowHelpException) {
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
        e.printAndExit("cliste")
    }
}
