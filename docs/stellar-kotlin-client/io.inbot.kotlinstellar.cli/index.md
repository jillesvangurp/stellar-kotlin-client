[stellar-kotlin-client](../index.md) / [io.inbot.kotlinstellar.cli](./index.md)

## Package io.inbot.kotlinstellar.cli

Cliste, the Command Line for Stellar. Convenient tool for using stellar from the command line that shows off the functionality provided in this library.

### Types

| Name | Summary |
|---|---|
| [CliSteArgs](-cli-ste-args/index.md) | `class CliSteArgs` |
| [CommandContext](-command-context/index.md) | `class CommandContext : `[`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html) |
| [CommandFunction](-command-function.md) | `typealias CommandFunction = (`[`CommandContext`](-command-context/index.md)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [Commands](-commands/index.md) | `enum class Commands` |
| [CommonArgs](-common-args/index.md) | `class CommonArgs` |
| [CreateAccountArgs](-create-account-args/index.md) | `class CreateAccountArgs` |
| [DefineAssetArgs](-define-asset-args/index.md) | `class DefineAssetArgs` |
| [DefineKeyArgs](-define-key-args/index.md) | `class DefineKeyArgs` |
| [HelpArgs](-help-args/index.md) | `class HelpArgs` |
| [NoArgs](-no-args/index.md) | `class NoArgs` |
| [PayArgs](-pay-args/index.md) | `class PayArgs` |
| [SetOptionsArgs](-set-options-args/index.md) | `class SetOptionsArgs` |
| [TradeAggsArgs](-trade-aggs-args/index.md) | `class TradeAggsArgs` |
| [TradeArgs](-trade-args/index.md) | `class TradeArgs` |
| [TrustAssetArgs](-trust-asset-args/index.md) | `class TrustAssetArgs` |
| [XdrArgs](-xdr-args/index.md) | `class XdrArgs` |

### Functions

| Name | Summary |
|---|---|
| [findCommandPos](find-command-pos.md) | `fun findCommandPos(args: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [main](main.md) | Command line tool to interact with stellar. Use this at your own risk.`fun main(args: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [renderHelp](render-help.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> renderHelp(clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<T>, commandName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [splitOnCommand](split-on-command.md) | `fun splitOnCommand(args: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>` |
| [withArgs](with-args.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> withArgs(args: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, block: T.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> withArgs(args: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, block: T.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
