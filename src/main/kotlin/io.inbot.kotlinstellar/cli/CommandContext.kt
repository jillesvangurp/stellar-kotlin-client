package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.SystemExitException
import io.inbot.kotlinstellar.KotlinStellarWrapper
import org.stellar.sdk.Asset
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

class CommandContext(val args: CliSteArgs) {
    private val pairInternal: KeyPair?
    val server: Server
    val wrapper: KotlinStellarWrapper
    val command by lazy { Commands.valueOf(args.commandName) }

    init {
        if(command.requiresKey) {
            if ("UNDEFINED" != args.secretKey) {

                pairInternal = KeyPair.fromSecretSeed(args.secretKey)
            } else if ("UNDEFINED" != args.publicKey) {
                pairInternal = KeyPair.fromAccountId(args.publicKey)
            } else {
                throw SystemExitException("You should specify either a secret or public key.", 1)
            }
        } else {
            pairInternal = null
        }
        server = Server(args.horizonUrl)
        wrapper = KotlinStellarWrapper(server)
    }
    val pair by lazy { pairInternal ?: throw SystemExitException("This operation requires a key pair",1) }

    fun run() {
        try {
            Commands.valueOf(args.commandName).command.invoke(this)
        } catch (e: IllegalArgumentException) {
            throw SystemExitException(
                "Command not supported. Should be one of: ${Commands.values().map { it.name }.joinToString(
                    ", "
                )}.", 1
            )
        }
    }

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