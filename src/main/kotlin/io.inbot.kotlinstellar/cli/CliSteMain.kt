package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.ShowHelpException
import kotlin.reflect.full.primaryConstructor

inline fun <reified T : Any> withArgs(args: Array<String>, block: T.() -> Unit) {
    @Suppress("UNCHECKED_CAST")
    val constructor = T::class.primaryConstructor as? (ArgParser) -> T
    if(constructor != null) {
        val t = ArgParser(args).parseInto(constructor)
        block.invoke(t)
    } else {
        throw IllegalStateException("cannot cast constructor for T: ${T::class.qualifiedName} must be T(ArgParser)")
    }
}

inline fun <reified T : Any> withArgs(args: List<String>, block: T.() -> Unit) {
    withArgs(args.toTypedArray(), block)
}

fun main(args: Array<String>) {
    try {
        val cliSteArgs = ArgParser(args).parseInto(::CliSteArgs)
        CommandContext(cliSteArgs).run()
    } catch (e: ShowHelpException) {
        e.printAndExit("cliste")
    }
}
