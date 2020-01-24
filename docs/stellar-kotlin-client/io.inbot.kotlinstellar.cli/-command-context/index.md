[stellar-kotlin-client](../../index.md) / [io.inbot.kotlinstellar.cli](../index.md) / [CommandContext](./index.md)

# CommandContext

`class CommandContext : `[`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `CommandContext(args: `[`CliSteArgs`](../-cli-ste-args/index.md)`, commandArgs: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)` |

### Properties

| Name | Summary |
|---|---|
| [accountKeyPair](account-key-pair.md) | `val accountKeyPair: KeyPair` |
| [args](args.md) | `val args: `[`CliSteArgs`](../-cli-ste-args/index.md) |
| [command](command.md) | `val command: `[`Commands`](../-commands/index.md) |
| [commandArgs](command-args.md) | `val commandArgs: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |
| [hasAccountKeyPair](has-account-key-pair.md) | `val hasAccountKeyPair: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [server](server.md) | `val server: Server` |
| [signers](signers.md) | `val signers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<KeyPair>` |
| [wrapper](wrapper.md) | `val wrapper: `[`KotlinStellarWrapper`](../../io.inbot.kotlinstellar/-kotlin-stellar-wrapper/index.md) |

### Functions

| Name | Summary |
|---|---|
| [asset](asset.md) | `fun asset(code: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): Asset` |
| [close](close.md) | `fun close(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [parseOrLookupKeyPair](parse-or-lookup-key-pair.md) | `fun parseOrLookupKeyPair(str: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): KeyPair?` |
| [parseOrLookupKeyPairAndValidate](parse-or-lookup-key-pair-and-validate.md) | `fun parseOrLookupKeyPairAndValidate(str: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): KeyPair` |
| [run](run.md) | `fun run(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [save](save.md) | `fun save(properties: `[`Properties`](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html)`, fileName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
