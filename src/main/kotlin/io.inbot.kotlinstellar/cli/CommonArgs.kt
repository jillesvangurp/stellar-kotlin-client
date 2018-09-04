package io.inbot.kotlinstellar.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

open class CommonArgs(parser: ArgParser) {
    val limit by parser.storing("-l", "--limit", help = "Limit", transform = { toInt() }).default(200)
}