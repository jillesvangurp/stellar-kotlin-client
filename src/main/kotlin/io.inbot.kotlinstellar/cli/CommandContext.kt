package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.SystemExitException
import io.inbot.kotlinstellar.KotlinStellarWrapper
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server

class CommandContext(val args: CliSteArgs) {
    val pair: KeyPair
    val server: Server
    val wrapper: KotlinStellarWrapper

    init {
        if ("UNDEFINED" != args.secretKey) {

            pair = KeyPair.fromSecretSeed(args.secretKey)
        } else if ("UNDEFINED" != args.publicKey) {
            pair = KeyPair.fromAccountId(args.publicKey)
        } else {
            throw SystemExitException("You should specify either a secret or public key.", 1)
        }
        server = Server(args.horizonUrl)
        wrapper = KotlinStellarWrapper(server)
    }

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
}