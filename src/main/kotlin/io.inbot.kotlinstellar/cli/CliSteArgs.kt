package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

private const val defaultUrl = "https://horizon-testnet.stellar.org"

class CliSteArgs(parser: ArgParser) {
    val secretKey by parser.storing(
        "-s", "--secret-key",
        help = """Secret key of the account signing the transaction.
            |Required for any commands that do transactions.
            |
            |Defaults to the value of the ST_SECRET_KEY environment variable.
            |""".trimMargin()
    )
        .default(System.getenv("ST_SECRET_KEY") ?: "UNDEFINED")
    val publicKey by parser.storing(
        "-p", "--public-key",
        help = """Public key of the account.
            |Only needed for read only commands. Not needed if you provide a private key.
            |
            |Defaults to the value of the ST_PUBLIC_KEY environment variable.
            |""".trimMargin()
    )
        .default(System.getenv("ST_PUBLIC_KEY") ?: "UNDEFINED")

    val horizonUrl by parser.storing(
        "-u", "--horizon-url",
        help = "URL for horizon. Defaults to $defaultUrl"
    ).default("$defaultUrl")
    val commandName by parser.positional("command. one of [balance]").default("balance")
    val commandArgs by parser.positionalList(
        help = "Zero or more args for the command, as required for each command.",
        sizeRange = 0..Int.MAX_VALUE
    )
}