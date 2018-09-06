package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.SystemExitException
import io.inbot.kotlinstellar.KotlinStellarWrapper
import org.stellar.sdk.Asset
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.parseKeyPair
import org.stellar.sdk.seedString
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

class CommandContext(val args: CliSteArgs) {
    private val pairInternal: KeyPair?
    val server: Server
    val wrapper: KotlinStellarWrapper
    val command by lazy {
        try {
            Commands.valueOf(args.commandName)
        } catch (e: Exception) {
            throw SystemExitException("invalid command name ${args.commandName}",1)
        }
    }

    init {
        pairInternal = parseOrLookupKeyPair(args.signKey)
        if(command.requiresKey && pairInternal == null) {
            throw SystemExitException("You should specify either a secret or public key.", 1)
        }
        server = Server(args.horizonUrl)
        wrapper = KotlinStellarWrapper(server)
    }
    val hashSigningKey by lazy { pairInternal != null }
    val signingKey by lazy { pairInternal ?: throw SystemExitException("Operation ${args.commandName} requires a key pair",1) }


    fun run() {
        try {
            if(args.verbose) {
                println(args)
                if(hashSigningKey) {
                    println("signing key: ${signingKey.seedString()}")
                }
                println(args.commandArgs.joinToString (",") )
                println(args.commandName)
            }
            Commands.valueOf(args.commandName).command.invoke(this)
        } catch (e: IllegalArgumentException) {
            if(args.verbose) {
                e.printStackTrace()
            }
            throw SystemExitException(
                "Command not supported. Should be one of: ${Commands.values().map { it.name }.joinToString(
                    ", "
                )}.", 1
            )
        }
    }

    fun parseOrLookupKeyPair(str: String) = parseKeyPair(args.keyProperties[str]?.toString()) ?: parseKeyPair(str)

    fun save(properties: Properties, fileName: String) {
        properties.store(FileOutputStream(File(fileName)), "assetcode -> issue address")
    }

    fun asset(code:String) : Asset {
        if(code == "XLM" || code == "native") {
            return AssetTypeNative()
        }
        val issuer = args.assetProperties.getProperty(code)
        if(issuer == null) {
            throw IllegalArgumentException("no such asset defined. Use cliste ${Commands.defineAsset.name} to define the asset")
        } else {
            return Asset.createNonNativeAsset(code, KeyPair.fromAccountId(issuer))
        }
    }
}