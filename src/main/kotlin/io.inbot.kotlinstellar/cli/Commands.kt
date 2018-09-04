package io.inbot.kotlinstellar.cli

import io.inbot.kotlinstellar.assetCode
import io.inbot.kotlinstellar.describe

enum class Commands(val command: (CommandContext) -> Unit) {
    balance({ context ->
        println(context.wrapper.server.accounts().account(context.pair).describe())
    }),
    offers({context ->
        withArgs<CommonArgs>(context.args.commandArgs) {
            println("Offers")
            println(context.server.offers().forAccount(context.pair).limit(limit).execute().records.map {
                "${it.seller.accountId}: ${it.amount} ${it.selling.assetCode} for ${it.buying.assetCode} at ${it.price}"
            }.joinToString("\n"))
        }
    })
}