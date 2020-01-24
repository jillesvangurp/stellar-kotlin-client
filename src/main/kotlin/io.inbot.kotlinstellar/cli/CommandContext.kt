package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.SystemExitException
import io.inbot.kotlinstellar.KotlinStellarWrapper
import io.inbot.kotlinstellar.StellarNetwork
import java.io.File
import java.io.FileOutputStream
import java.util.Properties
import org.stellar.sdk.Asset
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Network
import org.stellar.sdk.Server
import org.stellar.sdk.parseKeyPair
import org.stellar.sdk.requests.ErrorResponse

class CommandContext(val args: CliSteArgs, val commandArgs: Array<String>) : AutoCloseable {

    private val accountKeyPairInternal: KeyPair?
    val server: Server
    val wrapper: KotlinStellarWrapper
    val command by lazy {
        try {
            Commands.valueOf(args.commandName)
        } catch (e: Exception) {
            throw SystemExitException(
                "Command '${args.commandName}' not supported. Should be one of: ${Commands.values().map { it.name }.joinToString(
                    ", "
                )}.", 1
            )
        }
    }

    init {
        if (command.requiresAccount && args.accountKey == null) {
            throw SystemExitException("You should specify --account-key.", 1)
        } else {
            if (args.accountKey != null) {
                accountKeyPairInternal = parseOrLookupKeyPairAndValidate(args.accountKey!!)
            } else {
                accountKeyPairInternal = null
            }
        }
        server = Server(args.horizonUrl)
        val network = when (args.stellarNetwork) {
            StellarNetwork.public -> Network.PUBLIC
            StellarNetwork.testnet -> Network.TESTNET
            StellarNetwork.standalone -> Network(args.standAloneNetworkPassphrase)
        }

        wrapper = KotlinStellarWrapper(server, network = network)
    }
    val hasAccountKeyPair by lazy { accountKeyPairInternal != null }
    val accountKeyPair by lazy { accountKeyPairInternal ?: throw SystemExitException("Operation ${args.commandName} requires --account-key", 1) }

    val signers by lazy { args.signerKeys.map { k -> parseOrLookupKeyPairAndValidate(k!!) }.toTypedArray() }
    fun run() {
        try {
            if (args.verbose) {
                println("Parsed Arguments: ")
                println(args)
                println("-----------------------")
            }
            Commands.valueOf(args.commandName).command.invoke(this)
        } catch (e: SystemExitException) {
            if (args.verbose) {
                e.printStackTrace()
            }
            throw e
        } catch (err: ErrorResponse) {
            if (args.verbose) {
                err.printStackTrace()
            }
            throw SystemExitException(
                "Command '${args.commandName}' failed with status ${err.code}: ${err.message}\n${err.body}", 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw SystemExitException(
                "Problem running '${args.commandName}'. ${e.message}", 1
            )
        }
    }

    fun parseOrLookupKeyPair(str: String) = parseKeyPair(args.keyProperties[str]?.toString()) ?: parseKeyPair(str)
    fun parseOrLookupKeyPairAndValidate(str: String) = parseKeyPair(args.keyProperties[str]?.toString()) ?: parseKeyPair(str) ?: throw SystemExitException("invalid account $str", 1)

    fun save(properties: Properties, fileName: String) {
        properties.store(FileOutputStream(File(fileName)), "assetcode -> issue address")
    }

    fun asset(code: String): Asset {
        if (code == "XLM" || code == "native") {
            return AssetTypeNative()
        }
        val issuer = args.assetProperties.getProperty(code)
        if (issuer == null) {
            throw IllegalArgumentException("no such asset defined. Use cliste ${Commands.defineAsset.name} to define the asset")
        } else {
            return Asset.createNonNativeAsset(code, issuer)
        }
    }

    override fun close() {
        this.server.httpClient.connectionPool().evictAll()
    }
}
