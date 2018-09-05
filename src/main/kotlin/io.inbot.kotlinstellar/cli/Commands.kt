package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import io.inbot.kotlinstellar.TokenAmount
import io.inbot.kotlinstellar.assetCode
import io.inbot.kotlinstellar.describe
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import java.util.Locale
import kotlin.reflect.KClass

typealias CommandFunction = (CommandContext) -> Unit

private val doBalance: CommandFunction = { context ->
    println(context.wrapper.server.accounts().account(context.pair).describe())
}

class NoArgs(@Suppress("UNUSED_PARAMETER") parser: ArgParser) {
}


class CommonArgs(parser: ArgParser) {
    val limit by parser.storing("-l", "--limit", help = "Limit", transform = { toInt() }).default(200)
}

private val doOffers: CommandFunction = { context ->
    withArgs<CommonArgs>(context.args.commandArgs) {
        println(context.server.offers().forAccount(context.pair).limit(limit).execute().records.map {
            "${it.seller.accountId}: ${it.amount} ${it.selling.assetCode} for ${it.buying.assetCode} at ${it.price}"
        }.joinToString("\n"))
    }
}

class HelpArgs(parser: ArgParser) {
    val command by parser.positional(help = "name of the command").default("all")
}

private val doHelp: CommandFunction = { context ->
    withArgs<HelpArgs>(context.args.commandArgs) {
        if(command == "all") {
            println("Usage cliste [${Commands.values().joinToString(",")}] arguments...")
            println()
            println(Commands.values().map { it.helpText }.joinToString("\n"))
        } else {
            try {
                println(Commands.valueOf(command).helpText)
            } catch (e: IllegalArgumentException) {
                println("No such command $command")
            }
        }
    }
}

class DefineAssetArgs(parser: ArgParser) {
    val assetCode by parser.positional("4 or 12 letter asset code")
    val issuer by parser.positional("public key of the issuer")
}

private val doDefineAsset: CommandFunction = { context ->
    withArgs<DefineAssetArgs>(context.args.commandArgs) {
        val keyPair = KeyPair.fromAccountId(issuer) // validate public key
        Asset.createNonNativeAsset(assetCode, keyPair) // validate we can create the asset
        context.args.assetProperties.put(assetCode, issuer)
        context.saveAssetProperties()
    }
}

class CreateAccountArgs(parser: ArgParser) {
    val amount by parser.storing("Amount XML to be transferred to the new account (default 20)").default("20")
}

private val doCreateAccount: CommandFunction = { context ->
    withArgs<CreateAccountArgs>(context.args.commandArgs) {
        val created = context.wrapper.createAccount(TokenAmount.of(amount), sourceAccount = context.pair)
        println("created account with secret key ${created.secretSeed}")
    }
}

enum class Commands(
    val command: CommandFunction,
    val clazz: KClass<*> = NoArgs::class,
    val requiresKey: Boolean = true,
    val helpIntroduction: String=""
) {
    balance(doBalance, helpIntroduction = "Shows the account balance of the specified public key."),
    offers(doOffers, CommonArgs::class),
    defineAsset(doDefineAsset, DefineAssetArgs::class),
    createAccount(doCreateAccount, CreateAccountArgs::class),
    help(doHelp, HelpArgs::class, false,"Show help for a specific command")
    ;

    val helpText by lazy {
        """${name.toUpperCase(Locale.ROOT)}

${if (helpIntroduction.length > 0) helpIntroduction + "\n\n" else ""}${renderHelp(clazz, "cliste $name")}"""
    }
}