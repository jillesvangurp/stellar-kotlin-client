[![](https://jitpack.io/v/Inbot/inbot-stellar-kotlin-wrapper.svg)](https://jitpack.io/#Inbot/inbot-stellar-kotlin-wrapper)

# Inbot Stellar Kotlin Wrapper

The Inbot Stellar Kotlin Wrapper wraps the official Stellar java sdk with some Kotlin specific goodness. The intent is to get rid of a lot of boilerplate and make it easy to create accounts, work with a standalone container, and do transactions.

**This is a work in progress.** APIs may change, refactoring may happen, etc. When this changes we'll remove this notice.

# Examples

Create a custom asset with a trust line as per the [walkthrough](https://www.stellar.org/developers/guides/walkthroughs/custom-assets.html).

```kotlin
val server = Server("http://localhost:8000")
// create the wrapper
val wrapper = KotlinStellarWrapper(server)

val sourcePair=KeyPair.fromSecretSeed("SDDPXCR2SO7SUTV4JBQHLWQOP7DPDDRF7XL3GVPQKE6ZINHAIX4ZZFIH")
val issuerPair=KeyPair.fromSecretSeed("SBD2WR6L5XTRLBWCJJESXZ26RG4JL3SWKM4LASPJCJE4PSOHNDY3KHL4")
val distributionPair=KeyPair.fromSecretSeed("SC26JT6JWGTPO723TH5HZDUPUJQVWF32GKDEOZ5AFM6XQMPZQ4X5HJPG")

val bpAss = Asset.createNonNativeAsset("BrownyPoint", issuerPair.toPublicPair())
val tokenCap = TokenAmount.of(LongMath.pow(10,10),0)

// use standalone network password to figure out the root account and create an account
wrapper.createAccount(amountLumen = TokenAmount.of(1000,0), newAccount = sourcePair)
// use the minimum amount because we'll lock this account down after issueing
// + 1 because the transfer will drop us below the minimum amount
// TODO figure out the absolute minimums in stroops here
wrapper.createAccount(amountLumen = TokenAmount.of(100,0), sourceAccount = sourcePair, newAccount = issuerPair)
wrapper.createAccount(amountLumen = TokenAmount.of(100,0), sourceAccount = sourcePair, newAccount = distributionPair)
wrapper.trustAsset(distributionPair, bpAss, tokenCap)
// issue the tokens
wrapper.pay(bpAss, issuerPair, distributionPair,tokenCap)

wrapper.setHomeDomain(issuerPair,"browniepoints.com")
// prevent the issuer from ever issueing more tokens
val proofTheIssuerCanIssueNoMore = wrapper.lockoutAccount(issuerPair)
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
