package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import io.inbot.kotlinstellar.TokenAmount
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import org.stellar.sdk.assetCode
import org.stellar.sdk.responses.describe
import java.time.Instant
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
        context.save(context.args.assetProperties, context.args.assetPropertiesFileName)
    }
}

class DefineKeyArgs(parser: ArgParser) {
    val name by parser.positional("name of the key")
    val key by parser.positional("key")
}

private val doListAssets: CommandFunction = {
    println("Defined assets (${it.args.assetProperties.size}):")
    it.args.assetProperties.forEach({p -> println("${p.key}\t\t${p.value}")})
}

private val doDefineKey: CommandFunction = { context ->
    withArgs<DefineKeyArgs>(context.args.commandArgs) {
            KeyPair.fromAccountId(key) // validate key
        context.args.assetProperties.put(name, key)
        context.save(context.args.keyProperties,context.args.keyPropertiesFileName)
    }
}

private val doListKeys: CommandFunction = {
    println("Defined keys (${it.args.keyProperties.size}):")
    it.args.keyProperties.forEach({p -> println("${p.key}: ${p.value}")})
}


class CreateAccountArgs(parser: ArgParser) {
    val amount by parser.storing("Amount XML to be transferred to the new account (default 20)").default("20")
    val name by parser.storing("name under which to store the new key, defaults to key-<timestamp>").default("key-${Instant.now().toString()}")
}

private val doCreateAccount: CommandFunction = { context ->
    withArgs<CreateAccountArgs>(context.args.commandArgs) {
        val created = context.wrapper.createAccount(TokenAmount.of(amount), sourceAccount = context.pair)
        println("created account with secret key ${created.secretSeed}")
        context.args.keyProperties.put(name,created.secretSeed)
        context.save(context.args.keyProperties,context.args.keyPropertiesFileName)
    }
}

class PayArgs(parser: ArgParser) {
    val receiver by parser.positional("Receiver account key or name")
    val amount by parser.positional("Amount you are paying")
    val assetCode by parser.positional("Asset that you are paying with")
    val memo by parser.positional("Optional memo").default("")

}

private val doPay: CommandFunction = {context ->
    withArgs<PayArgs>(context.args.commandArgs) {
        val asset = context.asset(assetCode)
        context.wrapper.pay(asset, context.pair, KeyPair.fromAccountId(receiver),TokenAmount.of(amount),memo)
    }
}

enum class Commands(
    val command: CommandFunction,
    val clazz: KClass<*> = NoArgs::class,
    val helpIntroduction: String = "",
    val requiresKey: Boolean = true
) {
    balance(doBalance, helpIntroduction = "Shows the account balance of the specified public key."),
    offers(doOffers, CommonArgs::class),
    defineAsset(doDefineAsset, DefineAssetArgs::class),
    listAssets(doListAssets,NoArgs::class, "List the defined assets"),
    defineKey(doDefineAsset, DefineKeyArgs::class),
    listKeys(doListAssets,NoArgs::class, "List the defined keyss"),
    createAccount(doCreateAccount, CreateAccountArgs::class, helpIntroduction = "Create a new account"),
    help(doHelp, HelpArgs::class, "Show help for a specific command", false)
    ;

    val helpText by lazy {
        """${name.toUpperCase(Locale.ROOT)}

${if (helpIntroduction.length > 0) helpIntroduction + "\n\n" else ""}${renderHelp(clazz, "cliste $name")}"""
    }
}