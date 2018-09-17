[![](https://jitpack.io/v/Inbot/inbot-stellar-kotlin-wrapper.svg)](https://jitpack.io/#Inbot/inbot-stellar-kotlin-wrapper)

# Inbot Stellar Kotlin Wrapper

The Inbot Stellar Kotlin Wrapper wraps the official Stellar java sdk with some Kotlin specific goodness. The intent is to get rid of a lot of boilerplate and make it easy to create accounts, work with a standalone container, and do transactions.

**This is a work in progress.** APIs may change, refactoring may happen, etc. When this changes we'll remove this notice. Keeping the example  below is a bit of a PITA, so refer to the tests and API documentation (gradle dokka) for the current state.

The plan is to keep on adding convenience methods. 

**IMPORTANT** This currently depends on a fork of the java SDK that we created for a [pull request](https://github.com/stellar/java-stellar-sdk/pull/132). This shades the dependencies and updates some of them as well.

# Features

- make transaction submission and error handling easier. Our `doTransaction` extension function for the Server class in the sdk does retries on timeout and throws a runtime exceptions if the the result is not successful.
- make dealing with assets, asset codes, and token amounts less painful. The SDK represents amounts as strings; Stellar represents amounts as Long values. So, 1 XLM (the 'native' token) is actually represented as 10^7 or 10M stroops. To make dealing with amounts and calculating rates correctly, we have added a `TokenAmount` data class. This does all the right things and comes with several factory methods so you can construct amounts from a String, a Double, or a pair of longs to represent tokens and stroops. We also add several extension methods to get instances of this class from relevant classes in the SDK
- `KotlinStellarWrapper` provides simple functions to do common things like creating new accounts, adding trust lines, doing payments, or making offers.

# CliSte - the command line interface for Stellar

As part of this library, we also included a command line interface that you can use to interact with stellar.

- create accounts
- do payments, offers, multi signature payments
- create your own assets
- manage lists of accounts and assets
- and more ...
- works against standalone net, testnet, and public net. For the standalong net, it has support for generating accounts using the root key. No friendbot required.

[CliSte Documentation](cliste.md)

# Code Examples

Create a custom asset with a trust line as per the [walkthrough](https://www.stellar.org/developers/guides/walkthroughs/custom-assets.html).

```kotlin
val server = Server("http://localhost:8000")
// create the wrapper
val wrapper = KotlinStellarWrapper(server)

val sourcePair = KeyPair.fromSecretSeed("SDDPXCR2SO7SUTV4JBQHLWQOP7DPDDRF7XL3GVPQKE6ZINHAIX4ZZFIH")
val issuerPair = KeyPair.fromSecretSeed("SBD2WR6L5XTRLBWCJJESXZ26RG4JL3SWKM4LASPJCJE4PSOHNDY3KHL4")
val distributionPair = KeyPair.fromSecretSeed("SC26JT6JWGTPO723TH5HZDUPUJQVWF32GKDEOZ5AFM6XQMPZQ4X5HJPG")
val bpt = Asset.createNonNativeAsset("bpt", issuerPair.toPublicPair())
val native = AssetTypeNative()

logger.info("bootstrapping brownie point token")
// we need enough tokens in the source account that we can create the other accounts
wrapper.createAccount(amountLumen = TokenAmount.of(100000, 0), newAccount = sourcePair)
// use the minimum amount because we'll lock this account down after issueing
// + 1 because the transfer will drop us below the minimum amount
// TODO figure out the absolute minimums in stroops here
wrapper.createAccount(amountLumen = TokenAmount.of(1000, 0), sourceAccount = sourcePair, newAccount = issuerPair)
wrapper.createAccount(amountLumen = TokenAmount.of(1000, 0), sourceAccount = sourcePair, newAccount = distributionPair)
wrapper.trustAsset(distributionPair, bpt, tokenCap)
// issue the tokens
wrapper.pay(bpt, issuerPair, distributionPair, tokenCap)

wrapper.setHomeDomain(issuerPair, "browniepoints.com")
// prevent the issuer from ever issueing more tokens
val proofTheIssuerCanIssueNoMore = wrapper.lockoutAccount(issuerPair)

proofTheIssuerCanIssueNoMore.getTransactionResult().result.results.forEach {
    println("${it.tr.discriminant.name} ${it.tr.setOptionsResult.discriminant.name} ")
}
logger.info(proofTheIssuerCanIssueNoMore.resultXdr)
```

# Maven/gradle

You can find release jars on [jitpack](https://jitpack.io/#Inbot/inbot-stellar-kotlin-wrapper). Tagged releases will show up there.

To build, you need docker and docker-compose installed. The gradle build uses a plugin to spin up the included docker-compose for the standalone network.

```
gradle build
```

# License

[MIT License](LICENSE)
