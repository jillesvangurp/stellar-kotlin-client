package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import io.inbot.kotlinstellar.StellarNetwork
import java.io.File
import java.io.FileInputStream
import java.util.Properties

private const val defaultUrl = "http://localhost:8000"

class CliSteArgs(parser: ArgParser) {
    val accountKey by parser.storing("-a","--account-key",
        help = """Account key of the account for the transaction.
            |Required for any commands that do transactions on accounts.
            |
            |Defaults to the value of the ST_ACCOUNT_KEY environment variable.
            |""".trimMargin()
    ).default<String?>(System.getenv("ST_ACCOUNT_KEY"))
    // cannot access accountKey in default arg for this; so using a lazy to get to the value after parsing is done
    private val signerKeysInternal by parser.adding("-s","--signing-key", help = "Signing key")
    val signerKeys by lazy {if(signerKeysInternal.size==0 && accountKey != null) listOf(accountKey) else signerKeysInternal}
    val verbose: Boolean by parser.flagging(
        "-v", "--verbose",
        help = """Verbose output""".trimMargin()
    )
        .default(true)

    val horizonUrl by parser.storing(
        "-u", "--horizon-url",
        help = "URL for horizon. Defaults to to the value of the ST_HORIZON_URL environment variable or $defaultUrl if that is empty"
    ).default(System.getenv("ST_HORIZON_URL") ?: defaultUrl)

    val assetPropertiesFileName by parser.storing(
        "--asset-properties",
        help = "Properties file with assets"
    ).default("assets.properties")

    val assetProperties by lazy {
        val props = Properties()
        val f = File(assetPropertiesFileName)
        if (f.exists()) {
            props.load(FileInputStream(f))
        }
        props
    }

    val keyPropertiesFileName by parser.storing(
        "--key-properties",
        help = "Properties file with named public or private keys"
    ).default("keys.properties")

    val keyProperties by lazy {
        val props = Properties()
        val f = File(keyPropertiesFileName)
        if (f.exists()) {
            props.load(FileInputStream(f))
        }
        props
    }
    val standAloneNetworkPassphrase by parser.storing("network password").default("Standalone Network ; February 2017")
    val stellarNetwork by parser.storing("--stellar-network", help = "", transform = {
        try {
            StellarNetwork.valueOf(this)
        } catch (e: IllegalArgumentException) {
            throw SystemExitException(
                "unsupported network, should be one of ${StellarNetwork.values().joinToString(", ")}",
                1
            )
        }
    }).default(StellarNetwork.standalone)
    val commandName by parser.positional("command").default("help")

    override fun toString(): String {
        return """horizon: $horizonUrl
            |networkPassphrase: $standAloneNetworkPassphrase
            |stellarNetwork: $stellarNetwork
            |accountKey: $accountKey
            |signingKeys: ${signerKeys.joinToString(", ")}
            |assetPropertiesFileName: $assetPropertiesFileName
            |keyPropertiesFileName: $keyPropertiesFileName
            |command: $commandName
        """.trimMargin()
    }
}