package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import io.inbot.kotlinstellar.assetCode
import io.inbot.kotlinstellar.describe

typealias CommandBody = (CommandContext) -> Unit

open class CommonArgs(parser: ArgParser) {
    val limit by parser.storing("-l", "--limit", help = "Limit", transform = { toInt() }).default(200)
}

class HelpArgs(parser: ArgParser) {
    val command by parser.positional(help = "name of the command").default("help")
}

private val doBalance: CommandBody = { context ->
    println(context.wrapper.server.accounts().account(context.pair).describe())
}


private val doOffers: CommandBody = { context ->
    withArgs<CommonArgs>(context.args.commandArgs) {
        println(context.server.offers().forAccount(context.pair).limit(limit).execute().records.map {
            "${it.seller.accountId}: ${it.amount} ${it.selling.assetCode} for ${it.buying.assetCode} at ${it.price}"
        }.joinToString("\n"))
    }
}

private val doHelp: CommandBody = { context ->
    withArgs<HelpArgs>(context.args.commandArgs) {
        println(Commands.valueOf(command).helpFunction.invoke())
    }
}

enum class Commands(val helpFunction: () -> String, val command: CommandBody) {
    balance({ "cliste balance" }, doBalance),
    offers({ renderHelp<CommonArgs>("cliste offers") }, doOffers),
    help({ renderHelp<HelpArgs>("cliste help") }, doHelp);
}