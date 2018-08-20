# Inbot Stellar Kotlin Wrapper

The Inbot Stellar Kotlin Wrapper wraps the official Stellar java sdk with some Kotlin specific goodness. The intent is to get rid of a lot of boilerplate and make it easy to create accounts, work with a standalone container, and do transactions.

**This is a work in progress.** APIs may change, refactoring may happen, etc. When this changes we'll remove this notice.

# Examples

Create a custom asset with a trust line and do a payment.

```kotlin
val server = Server("http://localhost:8000")
// create the wrapper
val wrapper = KotlinStellarWrapper(server)

// use standalone network password to figure out the root account and create an account
val brownyPointIssuer = wrapper.createNewAccount(100.0, "issuer")
val bpAss = Asset.createNonNativeAsset("BrownyPoints", brownyPointIssuer)

val anotherAccount = wrapper.createNewAccount(100.0, "receiver")

// create a trustline to the asset
wrapper.trustAsset(anotherAccount, bpAss, 100.0)

// transfer a small amount
wrapper.pay(bpAss, brownyPointIssuer, anotherAccount, 2.0)

// use extension functions added to account and balance to assert
assert(server.accounts().account(anotherAccount).balanceFor(bpAss)?.balanceAmount()).isEqualTo(2.0)

```

# Maven/gradle

You can find release jars on [jitpack](https://jitpack.io/#Inbot/inbot-stellar-kotlin-wrapper). Tagged releases will show up there.

# License

[MIT License](LICENSE)
