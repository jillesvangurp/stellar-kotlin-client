package io.inbot.kotlinstellar.cli

    import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File
import java.io.FileInputStream
import java.util.Properties

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
    val verbose: Boolean by parser.flagging(
        "-v", "--verbose",
        help = """Verbose output""".trimMargin()
    )
        .default(true)

    val horizonUrl by parser.storing(
        "-u", "--horizon-url",
        help = "URL for horizon. Defaults to to the value of the ST_HORIZON_URL environment variable or $defaultUrl if that is empty"
    ).default(System.getenv("ST_HORIZON_URL") ?:"$defaultUrl")

    val assetPropertiesFileName by parser.storing("-a","--asset-properties",help="Properties file with assets").default("assets.properties")

    val assetProperties by  lazy {
        val props=Properties()
        val f=File(assetPropertiesFileName)
        if(f.exists()) {
            props.load(FileInputStream(f))
        }
        props
    }

    val keyPropertiesFileName by parser.storing("-k","--key-properties",help="Properties file with named public or private keys").default("keys.properties")

    val keyProperties by  lazy {
        val props=Properties()
        val f=File(keyPropertiesFileName)
        if(f.exists()) {
            props.load(FileInputStream(f))
        }
        props
    }

    val commandName by parser.positional("command").default("help   ")
    val commandArgs by parser.positionalList(
        help = "command plus command specifics.",
        sizeRange = 0..Int.MAX_VALUE
    )


    override fun toString(): String {
        return """horizon: $horizonUrl
            |secretKey: $secretKey publicKey: $publicKey
            |assetPropertiesFileName: $assetPropertiesFileName
            |keyPropertiesFileName: $keyPropertiesFileName
            |command: $commandName
            |commandArgs: ${commandArgs.joinToString(" ")}
        """.trimMargin()
    }

}