package io.inbot.kotlinstellar.cli

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class CliSteMainTest {

    @Test
    fun shouldCalculaterCorrectOffset() {
        val args = arrayOf<String>("--foo", "-b", "pay", "-a", "-b")
        val pos = findCommandPos(args)
        pos shouldBe 2
    }

    @Test
    fun shouldSplitArgs() {
        splitOnCommand(arrayOf("-x", "-y", "pay", "foo", "-b")).first shouldBe arrayOf("-x", "-y", "pay")
        splitOnCommand(arrayOf("-x", "-y", "pay", "foo", "-b")).second shouldBe arrayOf("foo", "-b")
        splitOnCommand(arrayOf("-x", "-y", "pay")).first shouldBe arrayOf("-x", "-y", "pay")
        splitOnCommand(arrayOf("-x", "-y", "pay")).second shouldBe arrayOf()
        splitOnCommand(arrayOf("-x", "-y")).first shouldBe arrayOf("-x", "-y")
        splitOnCommand(arrayOf("-x", "-y")).second shouldBe arrayOf()
        splitOnCommand(arrayOf("help")).first shouldBe arrayOf("help")
        splitOnCommand(arrayOf("help")).second shouldBe arrayOf()
        splitOnCommand(arrayOf()).first shouldBe arrayOf()
        splitOnCommand(arrayOf()).second shouldBe arrayOf()
    }
}
