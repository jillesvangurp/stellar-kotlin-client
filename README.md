[![](https://jitpack.io/v/Inbot/inbot-stellar-kotlin-wrapper.svg)](https://jitpack.io/#Inbot/inbot-stellar-kotlin-wrapper)

# Inbot Stellar Kotlin Wrapper

The Inbot Stellar Kotlin Wrapper wraps the official Stellar java sdk with some Kotlin specific goodness. The main goal for the library is to get rid of a lot of boilerplate that is needed for using the Java SDK. This is done by leveraging features in Kotlin to add extension methods to the official APIs, introduce DSL style APIs for things like transactions, etc.

The plan is to keep on adding convenience methods. 


# Features

- make transaction submission and error handling easier. Our `doTransaction` extension function for the Server class in the sdk does retries on timeout and throws a runtime exceptions if the the result is not successful.
- make dealing with assets, asset codes, and token amounts less painful. The SDK represents amounts as strings; Stellar represents amounts as Long values. So, 1 XLM (the 'native' token) is actually represented as 10^7 or 10M stroops. To make dealing with amounts and calculating rates correctly, we have added a `TokenAmount` data class. This does all the right things and comes with several factory methods so you can construct amounts from a String, a Double, or a pair of longs to represent tokens and stroops. We also add several extension methods to get instances of this class from relevant classes in the SDK
- `KotlinStellarWrapper` provides simple functions to do common things like creating new accounts, adding trust lines, doing payments, or making offers.

# CliSte - the Command Line Interface for STEllar

As part of this library, we also included a **Command Line Interface for STEllar (CLISTE)** that you can use to interact with stellar. This started out as a demo app for the library and quickly became a very convenient tool for us to interact with stellar. Using UIs like stellar laboratory is comparatively tedious and slow.

- Fully supports working with Standalone stellar instances running on localhost. Most wallets do not support this at all.
- On the standalone net, you can bootstrap accounts without the friendbot.
- Easily switch between standalone, testnet, and public net.
- create accounts, do payments, offers, multi signature payments, list trades, and much more
- Easily create and manage your own assets, trustlines, etc.
- Manage lists of accounts and assets and use human readable aliases for your key accounts in cliste invocations. This is highly useful for scripting scenarios using a standalone net. We have used this for prototyping the stellar launch of our token. 
- and much more ...

Note. we will likely move cliste to its own repository after the 1.0 release of this library.

[CliSte Documentation](cliste.md)

# Code Examples

Create a custom asset with a trust line as per the [walkthrough](https://www.stellar.org/developers/guides/walkthroughs/custom-assets.html).

Note: also look at our integration [tests](https://github.com/Inbot/inbot-stellar-kotlin-wrapper/blob/master/src/test/kotlin/io.inbot.kotlinstellar/StellarWrapperTest.kt) for a more in debth impression of how useful this is.

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
