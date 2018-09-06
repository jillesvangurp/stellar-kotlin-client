package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import io.inbot.kotlinstellar.TokenAmount
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import org.stellar.sdk.assetCode
import org.stellar.sdk.parseKeyPair
import org.stellar.sdk.responses.describe
import org.stellar.sdk.seedString
import java.time.Instant
import java.util.Locale
import kotlin.reflect.KClass

typealias CommandFunction = (CommandContext) -> Unit

private val doBalance: CommandFunction = { context ->
    println(context.wrapper.server.accounts().account(context.signingKey).describe())
}

class NoArgs(@Suppress("UNUSED_PARAMETER") parser: ArgParser) {
}


class CommonArgs(parser: ArgParser) {
    val limit by parser.storing("-l", "--limit", help = "Limit", transform = { toInt() }).default(200)
}

private val doOffers: CommandFunction = { context ->
    withArgs<CommonArgs>(context.args.commandArgs) {
        println(context.server.offers().forAccount(context.signingKey).limit(limit).execute().records.map {
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
    val issuer by parser.positional("public key of the issuer")
    val assetCode by parser.positional("4 or 12 letter asset code")
}

private val doDefineAsset: CommandFunction = { context ->
    withArgs<DefineAssetArgs>(context.args.commandArgs) {
        val keyPair = context.parseOrLookupKeyPair(issuer) ?: throw IllegalArgumentException("$issuer key not found or malformed")
        Asset.createNonNativeAsset(assetCode, keyPair) // validate we can create the asset
        context.args.assetProperties.put(assetCode, keyPair.accountId)
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
    it.args.keyProperties.forEach({p ->
        val keyPair = parseKeyPair(p.value.toString())
        println("${p.key}: secretKey ${keyPair?.seedString()?.subSequence(0,6)}.... accountId: ${keyPair?.accountId}")})
}


class CreateAccountArgs(parser: ArgParser) {
    val name by parser.positional( "name under which to store the new key, defaults to key-<timestamp>").default("key-${Instant.now().toString()}")
    val amount by parser.positional("Amount XML to be transferred to the new account (default 20)").default("20")
}

private val doCreateAccount: CommandFunction = { context ->
    withArgs<CreateAccountArgs>(context.args.commandArgs) {
        // if no pair, it will try to bootstrap a pair
        val created = context.wrapper.createAccount(TokenAmount.of(amount), sourceAccount = if(context.hashSigningKey) context.signingKey else null)
        println("created account with secret key ${String(created.secretSeed)}")
        context.args.keyProperties.put(name,String(created.secretSeed))
        context.save(context.args.keyProperties,context.args.keyPropertiesFileName)
    }
}

class TrustAssetArgs(parser: ArgParser) {
    val assetCode by parser.positional("Asset that you want to trust. Must be defined in assets.properties")
    val amount by parser.positional("Amount you trust the asset with")
}

private val doTrustAsset: CommandFunction = { context ->
    withArgs<TrustAssetArgs>(context.args.commandArgs) {
        context.wrapper.trustAsset(context.signingKey, context.asset(assetCode), TokenAmount.of(amount))
    }
}

class PayArgs(parser: ArgParser) {
    val receiver by parser.positional("Receiver account key (public or secret) or key name in keys.properties")
    val amount by parser.positional("Amount you are paying")
    val assetCode by parser.positional("Asset that you are paying with")
    val memo by parser.positional("Optional text memo").default("")
}

private val doPay: CommandFunction = {context ->
    withArgs<PayArgs>(context.args.commandArgs) {
        val asset = context.asset(assetCode)
        val receiverKey = context.parseOrLookupKeyPair(receiver)
            ?: throw IllegalArgumentException("key not found in ${context.args.keyPropertiesFileName} or malformed key: $receiver")
        context.wrapper.pay(asset, context.signingKey, receiverKey,TokenAmount.of(amount),memo)
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
    defineAsset(doDefineAsset, DefineAssetArgs::class,requiresKey = false),
    listAssets(doListAssets,NoArgs::class, "List the defined assets",requiresKey = false),
    defineKey(doDefineKey, DefineKeyArgs::class,requiresKey = false),
    listKeys(doListKeys,NoArgs::class, "List the defined keys",requiresKey = false),
    createAccount(doCreateAccount, CreateAccountArgs::class, helpIntroduction = "Create a new account",requiresKey = false),
    pay(doPay, PayArgs::class,helpIntroduction = "Pay an amount to another account"),
    trust(doTrustAsset, TrustAssetArgs::class,helpIntroduction = "Trust an asset"),
    help(doHelp, HelpArgs::class, "Show help for a specific command", false)
    ;

    val helpText by lazy {
        """${name.toUpperCase(Locale.ROOT)}

${if (helpIntroduction.length > 0) helpIntroduction + "\n\n" else ""}${renderHelp(clazz, "cliste $name")}"""
    }
}