package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import io.inbot.kotlinstellar.TokenAmount
import io.inbot.kotlinstellar.TradeAggregationResolution
import io.inbot.kotlinstellar.xdrDecodeString
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.WordUtils
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Operation
import org.stellar.sdk.Transaction
import org.stellar.sdk.assetCode
import org.stellar.sdk.parseKeyPair
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.responses.describe
import org.stellar.sdk.seedString
import org.stellar.sdk.xdr.OperationType
import org.stellar.sdk.xdr.TransactionEnvelope
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import kotlin.reflect.KClass

typealias CommandFunction = (CommandContext) -> Unit

private val doBalance: CommandFunction = { context ->
    println(context.wrapper.server.accounts().account(context.accountKeyPair).describe())
}

class NoArgs(@Suppress("UNUSED_PARAMETER") parser: ArgParser)

class CommonArgs(parser: ArgParser) {
    val limit by parser.storing("-l", "--limit", help = "Limit", transform = { toInt() }).default(200)
}

private val doListOffers: CommandFunction = { context ->
    withArgs<CommonArgs>(context.commandArgs) {
        println(context.server.offers().forAccount(context.accountKeyPair).limit(limit).execute().records.map {
            "${it.seller.accountId}: ${it.amount} ${it.selling.assetCode} for ${it.buying.assetCode} at ${it.price}"
        }.joinToString("\n"))
    }
}

class HelpArgs(parser: ArgParser) {
    val command by parser.positional(help = "name of the command").default("all")
}

private val doHelp: CommandFunction = { context ->
    withArgs<HelpArgs>(context.commandArgs) {
        if (command == "all") {
            println("# CliSte -  the Command Line Interface for Stellar\n")
            println(renderHelp(CliSteArgs::class, "cliste"))
            println()
            println("# Commands\n")
            println(Commands.values().joinToString("\n") { it.helpText })

            println(
                WordUtils.wrap(
                    """# Configuring CliSte

You can configure cliste using two environment variables

- `CLISTE_OPTS` any jvm arguments to configure heap, garbage collection, etc. You should not need this normally.
- `CLISTE_ARGS` default arguments you want to pass to cliste (e.g. your signing key `-k MYKEY`)

Additionally, cliste uses two properties files that you can manage with cliste commands:

- `keys.properties`: a map of key alias to key. You can use either public or private key here. For any argument that takes a key in cliste you can also use the alias. When you do a `cliste createAccount` it will get saved here. You can also use `cliste defineKey` and `cliste listKeys`
- `assets.properties`: a map of asset code to issueing accountId. Use `cliste defineAsset` and `cliste listAssets` to manage
""",
                    120
                )
            )
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
    withArgs<DefineAssetArgs>(context.commandArgs) {
        val keyPair =
            context.parseOrLookupKeyPair(issuer) ?: throw IllegalArgumentException("$issuer key not found or malformed")
        Asset.createNonNativeAsset(assetCode, keyPair) // validate we can create the asset
        context.args.assetProperties.put(assetCode, keyPair.accountId)
        context.save(context.args.assetProperties, context.args.assetPropertiesFileName)
    }
}

private val doListAssets: CommandFunction = {
    println("Defined assets (${it.args.assetProperties.size}):")
    it.args.assetProperties.forEach({ p -> println("${p.key}\t\t${p.value}") })
}

class DefineKeyArgs(parser: ArgParser) {
    val name by parser.positional("name of the key")
    val key by parser.positional("key")
}

private val doDefineKey: CommandFunction = { context ->
    withArgs<DefineKeyArgs>(context.commandArgs) {
        parseKeyPair(key) // validate key
        context.args.keyProperties.put(name, key)
        context.save(context.args.keyProperties, context.args.keyPropertiesFileName)
    }
}

private val doListKeys: CommandFunction = {
    println("Defined keys (${it.args.keyProperties.size}):")
    it.args.keyProperties.forEach { p ->
        val keyPair = parseKeyPair(p.value.toString())
        println(
            "${p.key}: secretKey ${if (keyPair?.canSign() ?: false) keyPair?.seedString()?.subSequence(
                0,
                6
            ).toString() + "...." else "-"} accountId: ${keyPair?.accountId}"
        )
    }
}

class CreateAccountArgs(parser: ArgParser) {
    val name by parser.positional("name under which to store the new key, defaults to key-<timestamp>").default("key-${Instant.now()}")
    val amount by parser.positional("Amount XML to be transferred to the new account (default 20)").default("20")
}

private val doCreateAccount: CommandFunction = { context ->
    withArgs<CreateAccountArgs>(context.commandArgs) {
        val signers: Array<KeyPair>
        if (context.hasAccountKeyPair) {
            signers = arrayOf(context.accountKeyPair)
        } else {
            signers = arrayOf(context.wrapper.rootKeyPair)
        }
        // if no pair, it will try to bootstrap a pair
        val existingKeyPair = context.parseOrLookupKeyPair(name)
        val created = context.wrapper.createAccount(
            TokenAmount.of(amount),
            sourceAccount = if (context.hasAccountKeyPair) context.accountKeyPair else null,
            newAccount = existingKeyPair ?: KeyPair.random(),
            signers = signers
        )
        if (existingKeyPair == null) {
            println("created account with secret key ${String(created.secretSeed)}")
            context.args.keyProperties.put(name, String(created.secretSeed))
            context.save(context.args.keyProperties, context.args.keyPropertiesFileName)
        } else {
            println("funded ${existingKeyPair.accountId} with $amount XLM")
        }
    }
}

class TrustAssetArgs(parser: ArgParser) {
    val assetCode by parser.positional("Asset that you want to trust. Must be defined in assets.properties")
    val amount by parser.positional("Amount you trust the asset with").default<String?>(null)
}

private val doTrustAsset: CommandFunction = { context ->
    withArgs<TrustAssetArgs>(context.commandArgs) {
        context.wrapper.trustAsset(
            context.accountKeyPair,
            context.asset(assetCode),
            if (amount == null) TokenAmount.maxAmount else TokenAmount.of(amount!!),
            signers = context.signers
        )
    }
}

class PayArgs(parser: ArgParser) {
    val receiver by parser.positional("Receiver account key (public or secret) or key name in keys.properties")
    val amount by parser.positional("Amount you are paying")
    val assetCode by parser.positional("Asset that you are paying with")
    val memo by parser.positional("Optional text memo").default("")
}

private val doPay: CommandFunction = { context ->
    withArgs<PayArgs>(context.commandArgs) {
        val asset = context.asset(assetCode)
        val receiverKey = context.parseOrLookupKeyPair(receiver)
            ?: throw IllegalArgumentException("key not found in ${context.args.keyPropertiesFileName} or malformed key: $receiver")
        context.wrapper.pay(
            context.accountKeyPair,
            receiverKey,
            TokenAmount.of(amount),
            asset,
            memo,
            signers = context.signers
        )
    }
}

private val doPreparePaymentTX: CommandFunction = { context ->
    withArgs<PayArgs>(context.commandArgs) {
        val (hash, xdr) = context.wrapper.preparePaymentTransaction(
            context.accountKeyPair,
            context.parseOrLookupKeyPairAndValidate(receiver),
            TokenAmount.of(amount),
            context.asset(assetCode)
        )
        println("tx hash: $hash")
        println("tx envelope xdr: $xdr")
    }
}

class XdrArgs(parser: ArgParser) {
    val xdr by parser.positional(help = "Base64 xdr of the transaction envelope.")
}

private val doSignTx: CommandFunction = { context ->
    withArgs<XdrArgs>(context.commandArgs) {
        val tx = Transaction.fromEnvelopeXdr(xdrDecodeString(xdr, TransactionEnvelope::class))
        context.signers.forEach {
            tx.sign(it)
        }

        val hash = Base64.getEncoder().encode(tx.hash()).toString(StandardCharsets.UTF_8)
        println("tx hash: $hash")
        println("tx envelope xdr: ${tx.toEnvelopeXdrBase64()}")
    }
}

private fun stringify(o: Operation): String {
    val discriminant = o.toXdr().body.discriminant
    return when (discriminant) {
        OperationType.PAYMENT -> {
            val paymentOp = o.toXdr().body.paymentOp
            "${TokenAmount.ofStroops(paymentOp.amount.int64)} ${Asset.fromXdr(paymentOp.asset).assetCode} to ${KeyPair.fromXdrPublicKey(
                paymentOp.destination.accountID
            ).accountId}"
        }
        else -> "${o.toXdr().body.discriminant}"
    }
}

private val doTxInfo: CommandFunction = { context ->
    withArgs<XdrArgs>(context.commandArgs) {
        val tx = Transaction.fromEnvelopeXdr(xdrDecodeString(xdr, TransactionEnvelope::class))
        val ops = tx.operations
            .map { stringify(it) }
            .joinToString("\n")

        println(
            """${tx.sequenceNumber} operations:
            |source account: ${tx.sourceAccount.accountId}
            |$ops
            |Signatures:
            |${tx.signatures.map {
                Base64.getEncoder().encodeToString(it.signature.signature)
            }.joinToString("\n")}""".trimMargin()
        )
    }
}

private val doSubmitTx: CommandFunction = { context ->
    withArgs<XdrArgs>(context.commandArgs) {
        val tx = Transaction.fromEnvelopeXdr(xdrDecodeString(xdr, TransactionEnvelope::class))
        val txResponse = context.server.submitTransaction(tx)
        if (txResponse.isSuccess) {
            println("OK")
        } else {
            println(
                "Error response: ${txResponse.extras.resultCodes?.transactionResultCode} - ${txResponse.extras.resultCodes?.operationsResultCodes?.joinToString(
                    ", "
                )}"
            )
        }
    }
}

class SetOptionsArgs(parser: ArgParser) {
    val lowThreshold by parser.storing("--low-threshold", help = "", transform = { toInt() }).default<Int?>(null)
    val mediumThreshold by parser.storing("--medium-threshold", help = "", transform = { toInt() }).default<Int?>(null)
    val highThreshold by parser.storing("--high-threshold", help = "", transform = { toInt() }).default<Int?>(null)
    val masterKeyWeight by parser.storing("--master-key-weight", help = "", transform = { toInt() }).default<Int?>(null)
    val signerWeight by parser.storing("--signer-weight", help = "", transform = { toInt() }).default<Int?>(null)
    val signerKey by parser.storing("--signer-key", help = "").default<String?>(null)
    val homeDomain by parser.storing("--home-domain", help = "").default<String?>(null)
}

private val doSetOptions: CommandFunction = { context ->
    withArgs<SetOptionsArgs>(context.commandArgs) {
        context.wrapper.setAccountOptions(context.accountKeyPair, signers = context.signers) {
            if (lowThreshold != null) {
                setLowThreshold(lowThreshold!!)
            }
            if (mediumThreshold != null) {
                setMediumThreshold(mediumThreshold!!)
            }
            if (highThreshold != null) {
                setHighThreshold(highThreshold!!)
            }
            if (masterKeyWeight != null) {
                setMasterKeyWeight(masterKeyWeight!!)
            }
            if (StringUtils.isNotBlank(homeDomain)) {
                setHomeDomain(homeDomain)
            }
            if (StringUtils.isNotBlank(signerKey)) {
                if (signerWeight == null) throw SystemExitException(
                    "--signer-weight is required when adding a signer",
                    1
                )
                setSigner(context.parseOrLookupKeyPair(signerKey!!)?.xdrSignerKey, signerWeight)
            }
        }
    }
}

private val doListAllAssets: CommandFunction = { context ->
    println("code\tissuer\tamount\tnumber_of_accounts")
    context.wrapper.assetsSequence(sleepOnThrottle = 5000).forEach {
        println("${it.assetCode}\t${it.assetIssuer}\t${it.amount}\t${it.numAccounts}")
    }
}

class TradeArgs(parser: ArgParser) {
    val baseAsset by parser.positional("Base asset")
    val counterAsset by parser.positional("Counter asset")
}

private val doListPayments: CommandFunction = { context ->
    withArgs<NoArgs>(context.commandArgs) {
        runBlocking {
            println("timestamp\tfrom_account\tto_account\tamount\tasset\ttransaction_hash")
            context.wrapper.paymentSequence(context.accountKeyPair, cursor = "0", pollingIntervalMs = 200, fetchSize = 199).forEach { it ->
                println("${it.createdAt}\t${it.from.accountId}\t${it.to.accountId}\t${it.amount}\t${it.asset.assetCode}\t${it.transactionHash}")
            }
        }
    }
}

private val doListTrades: CommandFunction = { context ->
    withArgs<TradeArgs>(context.commandArgs) {
        val builder = context.server.trades()
            .baseAsset(context.asset(baseAsset))
            .counterAsset(context.asset(counterAsset))

        builder.order(RequestBuilder.Order.DESC).limit(199)
        val response = builder.execute()
        response.records.forEach {
            println(it.describe())
        }
    }
}

class TradeAggsArgs(parser: ArgParser) {
    val fromTime by parser.storing("--from", help = "From time in ms after epoch. Default to now-24h", transform = { toLong() })
        .default<Long>(System.currentTimeMillis() - 60*60*1000*24)
    val toTime by parser.storing("--to", help = "to time in ms after epoch. Default now", transform = { toLong() })
        .default<Long>(System.currentTimeMillis())
    val resolution by parser.storing("-r", "--resolution", help = "resolution. One of ${TradeAggregationResolution.validValues}").default("1_HOURS")
    val baseAsset by parser.positional("Base asset")
    val counterAsset by parser.positional("Counter asset")
    val offSet by parser.positional(help = "offset", transform = { toLong() }).default<Long>(0L)
}

private val doListTradeAggs: CommandFunction = { context ->
    withArgs<TradeAggsArgs>(context.commandArgs) {
        context.server.tradeAggregations(
            context.asset(baseAsset),
            context.asset(counterAsset),
            fromTime,
            toTime,
            TradeAggregationResolution.parse(resolution)?.resolution
                ?: throw InvalidArgumentException(
                    "Should be one of ${TradeAggregationResolution.validValues}"),
            offSet
        ).execute().records.forEach {
            println(it.describe(baseAsset, counterAsset))
        }
    }
}

private val doFeeStats: CommandFunction = { context ->
    withArgs<NoArgs>(context.commandArgs) {

        val feeStats = context.server.feeStats().execute()
        println("""
${feeStats.lastLedgerBaseFee}   last ledger base fee
${feeStats.ledgerCapacityUsage} ledger capacity usage
${feeStats.min} min
${feeStats.p10} p10
${feeStats.p20} p20
${feeStats.p30} p30
${feeStats.p40} p40
${feeStats.p50} p50
${feeStats.p60} p60
${feeStats.p70} p70
${feeStats.p80} p80
${feeStats.p90} p90
${feeStats.p95} p95
${feeStats.p99} p99

            """.trimIndent())
    }
}

enum class Commands(
    val command: CommandFunction,
    val clazz: KClass<*> = NoArgs::class,
    val helpIntroduction: String = "",
    val requiresAccount: Boolean = true
) {
    balance(doBalance, helpIntroduction = "Shows the account balance of the specified public key."),
    listOffers(doListOffers, CommonArgs::class),
    defineAsset(doDefineAsset, DefineAssetArgs::class, requiresAccount = false),
    listAssets(doListAssets, NoArgs::class, "List the defined assets", requiresAccount = false),
    defineKey(doDefineKey, DefineKeyArgs::class, requiresAccount = false),
    listKeys(doListKeys, NoArgs::class, "List the defined keys", requiresAccount = false),
    createAccount(
        doCreateAccount,
        CreateAccountArgs::class,
        helpIntroduction = "Create a new account",
        requiresAccount = false
    ),
    pay(doPay, PayArgs::class, helpIntroduction = "Pay an amount to another account"),
    preparePaymentTX(
        doPreparePaymentTX, PayArgs::class,
        helpIntroduction = """Prepare an XDR transaction for a payment. Prints the
        | XDR of the transaction envelope so you can send it to the signees.""".trimMargin()
    ),
    txInfo(
        doTxInfo,
        PayArgs::class,
        helpIntroduction = "Show information about an XDR transaction envelope.",
        requiresAccount = false
    ),
    signTx(
        doSignTx,
        PayArgs::class,
        helpIntroduction = "Add a signature to a transaction envelope in XDR form.",
        requiresAccount = false
    ),
    submitTx(
        doSubmitTx,
        PayArgs::class,
        helpIntroduction = "Submit a transaction envelope in XDR form. You should add signatures first using signTx.",
        requiresAccount = false
    ),
    listPayments(doListPayments, NoArgs::class, helpIntroduction = "List trades", requiresAccount = true),
    listTrades(doListTrades, TradeArgs::class, helpIntroduction = "List trades", requiresAccount = false),
    listTradeAggs(
        doListTradeAggs,
        TradeAggsArgs::class,
        helpIntroduction = "List trade aggregations",
        requiresAccount = false
    ),
    trust(doTrustAsset, TrustAssetArgs::class, helpIntroduction = "Trust an asset"),
    setOptions(doSetOptions, SetOptionsArgs::class, helpIntroduction = "Set options on an account"),
    listAssetsOnStellar(doListAllAssets, NoArgs::class, "lists all assets on stellar", false),
    feeStats(doFeeStats, NoArgs::class, "Show fee stats", false),
    help(doHelp, HelpArgs::class, "Show help for a specific command", false)
    ;

    val helpText by lazy {
        """## $name

${if (helpIntroduction.length > 0) WordUtils.wrap(helpIntroduction, 160) + "\n\n" else ""}${renderHelp(
            clazz,
            "cliste $name"
        )}"""
    }
}