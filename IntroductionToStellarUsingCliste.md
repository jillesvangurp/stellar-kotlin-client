---
title: Explaining Stellar using Cliste
published: false
description: Practical overview of some key stellar concepts using my Command Line Interface for STEllar (CLISTE).
tags: Stellar
---

# Origins of Cliste

A few months ago, we decided to create a token on Stellar at [Inbot](inbot.io). So, I read up, explored the sdks and started trying to figure out how everything works in the Stellar world.

Since I primarily use Kotlin these days, I decided that I wanted to adapt the official stellar sdk for Java and make it a bit more kotlin friendly. Kotlin and Java play really nice together so this basically boiled down to me creating a new Github project called [inbot-stellar-kotlin-wrapper](https://github.com/Inbot/inbot-stellar-kotlin-wrapper).

Initially I was just fooling around and trying to figure out different parts of the Horizon API. I  figured out how to run a standalone chain and wrote some code, added some tests, etc. After a few days I realized that

1. this stuff is easy
1. I really would like to use the command line instead of writing code and tests all the time.

So, I googled for a suitable command line argument parser for Kotlin and stumbled on com.xenomachina.kotlin-argparser. It's a really nice kotlin dsl for picking apart commandlines.

Next steps were basically, coming up with a nice name: Command Line Interface for STellar, aka. cliste. At this point, I've been working on this project on and off for about 2 months. It's getting to a state where it is pretty useful.

# Explaining Stellar using Cliste

Now that I have cliste, I can show some simple examples to explain how stellar works. Instead of giving you the usual marketing cliches, bad metaphores, and other verbose ways of communicating stuff, I'll highlight some key features using cliste:

## Running a standalone stellar

If you want to fool around a bit, firing up a standalone chain is the best way.

```
docker run --rm --name stellarstandalone -p "8000:8000"  stellar/quickstart --standalone
```

It will take a second to start. Since we pass `--rf` all your data will be lost if you kill the container. So, you can use this as a sandbox. You can swap out --standalone for --testnet or --public to target the testnet or public stellar net instead. 

# Creating accounts

Lets create an account and give ourselves some XLM. In stellar, accounts must have a minimum balance of 0.5 XLM. On the standalone chain in the quickstart image it is still 20 XLM though. So, to be safe. We give alice plenty.

Since we are on a standalone network, we'll have to create an account from nothing. This works as follows.

```
$ ./cliste createAccount alice 1000
17:35:56.715 [main] INFO io.inbot.kotlinstellar.KotlinStellarWrapper - using standalone network
17:35:58.166 [main] INFO org.stellar.sdk.KotlinExtensions - 7 07067baf191a516c6434c84c6232c3672dd179883451641b417fb3090bc82d08 success:true fee:100 CREATE_ACCOUNT
17:35:58.166 [main] INFO io.inbot.kotlinstellar.KotlinStellarWrapper - created GBJR3JH4ZC5LZKGM2PPSVMJFPPWREVEKD4TQJL2RMUHI75P35N4JVABC
created account with secret key SCBB6IXATC2RFKCFIFUUIXGD5QDHRYAEQZBDDDMPRO3PVYXEMETCC67F
```

What just happened here:
- we generated a new key pair 
- it was stored in keys.properties under the key alice. This allows us to use that as an alias in future commands. 
- we gave ourselves some XLM. We can do this because we know the seed of the chain. 
- it logged some details, like the private key

This won't work on testnet but you can use the [friendbot](https://www.stellar.org/laboratory/#account-creator?network=test) instead. On the public net, the only way to fund new accounts is through an exchange or via an existing account.

## Alice can create more accounts now

Now that alice has a valid account, she can create an account for bob without and fund the base XLM balance for bob herself.

```
$ ./cliste -a alice createAccount bob 50
17:40:07.888 [main] INFO org.stellar.sdk.KotlinExtensions - 57 e580f9e898a7151000e0228500d17846165aebcfed405411167cf5b1f41dd6b4 success:true fee:100 CREATE_ACCOUNT
17:40:07.892 [main] INFO io.inbot.kotlinstellar.KotlinStellarWrapper - created GAURP3FQ56BOF2PXF5DCLFS4QOJPTO5XM62G2AAMAQXB4PZWYNSS4RKA
created account with secret key SCYD6JC5GHFW63T5KTNVOT5FFJIW2R67VBEHFYIQZCMS2VLLBAKUI4YW
```

So we simply add alice's key to the command with -a and define a new key.

## Managing accounts

If you want to know what keys you have you can list them as follows:

```
$ ./cliste listKeys
Defined keys (2):
alice: secretKey SCBB6I.... accountId: GBJR3JH4ZC5LZKGM2PPSVMJFPPWREVEKD4TQJL2RMUHI75P35N4JVABC
bob: secretKey SCYD6J.... accountId: GAURP3FQ56BOF2PXF5DCLFS4QOJPTO5XM62G2AAMAQXB4PZWYNSS4RKA
```

The secret keys are abbreviated here. 

Note, you can also manually add keys in keys.properties. You can also mix public keys and private keys here. So, you can use this like an address book for public keys that you car about. You only need private keys if you are going to do transactions for the account.

**Be careful storing private keys** you care about here. This tool is intended as a development tool only. I may add some more suitable protection in the future but right now it is all plain text. In a nutshell, don't do anything with this that I would not do either.

# Payments and balances

Lets give bob some more XLM:

```
$ ./cliste -a alice pay bob 10 XLM
17:44:12.535 [main] INFO org.stellar.sdk.KotlinExtensions - 106 703c34d98162f5223805cdd54a48261ae73e0dae8d5fccc76caba59d492f228a success:true fee:100 PAYMENT
```

Bob and alice can each check their balance:
```
$ ./cliste -a alice balance
accountId: GBJR3JH4ZC5LZKGM2PPSVMJFPPWREVEKD4TQJL2RMUHI75P35N4JVABC subEntryCount: 0 home domain: null

thresholds: 0 0 0
signers:
	GBJR3JH4ZC5LZKGM2PPSVMJFPPWREVEKD4TQJL2RMUHI75P35N4JVABC 1
authRequired: false
authRevocable: false

Balances:
XLM b:939.9999800 l:- - sl: - - bl: -

$ ./cliste -a bob balance
accountId: GAURP3FQ56BOF2PXF5DCLFS4QOJPTO5XM62G2AAMAQXB4PZWYNSS4RKA subEntryCount: 0 home domain: null

thresholds: 0 0 0
signers:
	GAURP3FQ56BOF2PXF5DCLFS4QOJPTO5XM62G2AAMAQXB4PZWYNSS4RKA 1
authRequired: false
authRevocable: false

Balances:
XLM b:60.0000000 l:- - sl: - - bl: -
```

This returns a bit of meta data and a list of balances that currently only includes XLM.

So, Bob, now has exactly 60 XLM. Alice has payed 50 and 10 XLM as well as some fees for both transactions: 2x 100 stroops. A stroop is 1/100000th of an XLM. This means you can fund 100K transactions with a mere 1 XLM, which at the time of writing is roughly 20 euro cents.

# Issuing your own token

To issue a new token, we need an issuing account. Lets create that as well as a distribution account that we will use for distributing our FOO token. Alice gets to pick up the bill for funding these accounts as well.

```
$ ./cliste -a alice createAccount issuing 100
$ ./cliste -a alice createAccount distribution 100
```

# Token management

Now lets define our token:
```
$ ./cliste defineAsset issuing FOO

$ ./cliste listAssets
Defined assets (1):
FOO		GBSR46WQNCDK7SPUW3XR3C663AE3QDG3MGG5S66GI6XQINQYGAZH7CF5
```

This does nothing else than add another alias to a file called assets.properties. In stellar assets are always identified by their code + the account that issued it.

We will issue FOO from our issuing account to our distribution account. 

# Trust lines

For this to work there needs to be a trustline. In stellar, you can only hold tokens that you trust. So for our distribution account to be able to hold FOO, it needs to trust FOO

```
$ ./cliste -a distribution trust FOO
17:58:07.565 [main] INFO org.stellar.sdk.KotlinExtensions - 273 f5ad81be61734ebfaef89233e5203a9f24cad1538312b17607b047da9de15a62 success:true fee:100 CHANGE_TRUST
```

If we now check the balance for the distribution account we'll see a new entry under balances:

```
$ ./cliste -a distribution balance
accountId: GADRJ4IDLVMOFE5DNQF7UZGWAUABKO7MYM2FAKGEGVPZA2W4RFQFF6ZQ subEntryCount: 1 home domain: null

thresholds: 0 0 0
signers:
	GADRJ4IDLVMOFE5DNQF7UZGWAUABKO7MYM2FAKGEGVPZA2W4RFQFF6ZQ 1
authRequired: false
authRevocable: false

Balances:
FOO (GBSR46WQNCDK7SPUW3XR3C663AE3QDG3MGG5S66GI6XQINQYGAZH7CF5) b:0.0000000 l:922337203685.4775807 - sl: - - bl: -
XLM b:99.9999900 l:- - sl: - - bl: -
```

So, our distribution account 'trusts' FOO to the extent of Long.MAX stroops of a FOO or about 922337203685.4775807 FOO. This is the maximum value you can put in stellar's 64 bit balance. If you want you can actually limit your trust to something smaller.

## Magic happens ...

Now lets do a magic trick and make our distribution account owner a billionaire (in FOO):

```
$ ./cliste -a issuing pay distribution 10000000000 FOO
18:01:57.355 [main] INFO org.stellar.sdk.KotlinExtensions - 319 11d1f4533656ee7b7e009f6db8ad9c61159bd4012790ffc46eff7ed03000a85d success:true fee:100 PAYMENT
```

Simply paying some FOO from the issuing account actually causes the FOO coin to come into existence. If we check the balance, we'll see simply paying from issuing to distribution created the token:

```
$ ./cliste -a distribution balance
accountId: GADRJ4IDLVMOFE5DNQF7UZGWAUABKO7MYM2FAKGEGVPZA2W4RFQFF6ZQ subEntryCount: 1 home domain: null

thresholds: 0 0 0
signers:
	GADRJ4IDLVMOFE5DNQF7UZGWAUABKO7MYM2FAKGEGVPZA2W4RFQFF6ZQ 1
authRequired: false
authRevocable: false

Balances:
FOO (GBSR46WQNCDK7SPUW3XR3C663AE3QDG3MGG5S66GI6XQINQYGAZH7CF5) b:10000000000.0000000 l:922337203685.4775807 - sl: - - bl: -
XLM b:99.9999900 l:- - sl: - - bl: -
```

# Multi signatures

Of course in practice you might want to lock things down a bit. For this we can modify the account options. A good practice is to protect important accounts with mutliple signatures.

## Adding signees to the issuing account

Lets add alice and bob as a signees to the issuing account.

```
$ ./cliste -a issuing setOptions --signer-key alice --signer-weight 5
18:07:07.987 [main] INFO org.stellar.sdk.KotlinExtensions - 381 78d4572ef51900f088babf87a4e878771391950a24ca6f710a1deacde0967ad7 success:true fee:100 SET_OPTIONS

$ ./cliste -a issuing setOptions --signer-key bob --signer-weight 5
18:07:17.933 [main] INFO org.stellar.sdk.KotlinExtensions - 383 5c7b0ee0747c57fbe456781888b322ef7d93270af9db8daf010c263ad64b5c38 success:true fee:100 SET_OPTIONS
```

This adds alice and bob as signers. Their keys have a weight of 5. If you look at the balance above, you'll see it defaults to 0. So either alice or bob have enough weight to do everything.

## Managing thresholds and weights

Lets lock down the issuing account:

```
$ ./cliste -a issuing setOptions --low-threshold 8 --medium-threshold 8 --high-threshold 8 --master-key-weight 0
18:16:02.379 [main] INFO org.stellar.sdk.KotlinExtensions - 488 f1a347698c2b8ee6d9c891d6da7ae1c86fc367785b4f0dc1e71c54d78a7726e9 success:true fee:100 SET_OPTIONS

$ ./cliste -a issuing balance
accountId: GBSR46WQNCDK7SPUW3XR3C663AE3QDG3MGG5S66GI6XQINQYGAZH7CF5 subEntryCount: 2 home domain: null

thresholds: 8 8 8
signers:
	GBJR3JH4ZC5LZKGM2PPSVMJFPPWREVEKD4TQJL2RMUHI75P35N4JVABC 5
	GAURP3FQ56BOF2PXF5DCLFS4QOJPTO5XM62G2AAMAQXB4PZWYNSS4RKA 5
	GBSR46WQNCDK7SPUW3XR3C663AE3QDG3MGG5S66GI6XQINQYGAZH7CF5 0
authRequired: false
authRevocable: false

Balances:
XLM b:99.9999500 l:- - sl: - - bl: -
```

So, this confirms our issuing account now has 3 keys. We set the master key to 0; so it can no longer be used to issue FOO:

```
$ ./cliste -a issuing pay distribution 10000000000 FOO
java.lang.IllegalStateException: failure after 0 transaction failed tx_bad_auth - null
	at org.stellar.sdk.KotlinExtensionsKt.doTransactionInternal(KotlinExtensions.kt:180)
	at org.stellar.sdk.KotlinExtensionsKt.doTransaction(KotlinExtensions.kt:142)
	at io.inbot.kotlinstellar.KotlinStellarWrapper.pay(KotlinStellarWrapper.kt:322)
	at io.inbot.kotlinstellar.KotlinStellarWrapper.pay$default(KotlinStellarWrapper.kt:314)
	at io.inbot.kotlinstellar.cli.CommandsKt$doPay$1.invoke(Commands.kt:194)
	at io.inbot.kotlinstellar.cli.CommandsKt$doPay$1.invoke(Commands.kt)
	at io.inbot.kotlinstellar.cli.CommandContext.run(CommandContext.kt:54)
	at io.inbot.kotlinstellar.cli.CliSteMainKt.main(CliSteMain.kt:88)
com.xenomachina.argparser.SystemExitException: Problem running 'pay'. failure after 0 transaction failed tx_bad_auth - null
	at io.inbot.kotlinstellar.cli.CommandContext.run(CommandContext.kt:62)
	at io.inbot.kotlinstellar.cli.CliSteMainKt.main(CliSteMain.kt:88)
cliste: Problem running 'pay'. failure after 0 transaction failed tx_bad_auth - null
```

## Signing transactions

To make this work, both Alice and Bob need to sign the transaction. So lets create an unsigned transaction:

```
$ ./cliste -a issuing preparePaymentTX distribution 10000000000 FOO
Transaction envelope xdr:
tx hash: zf+uG/7ePiTLuoqLbqgMMyQDq+PlxEsJkVEKq/jEixs=
tx envelope xdr: AAAAAGUeetBohq/J9LbvHYve2Am4DNthjdl7xkevBDYYMDJ/AAAAZAAAAMYAAAAGAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAABxTxA11Y4pOjbAv6ZNYFABU77MM0UCjENV+QatyJYFIAAAABRk9PAAAAAABlHnrQaIavyfS27x2L3tgJuAzbYY3Ze8ZHrwQ2GDAyfwFjRXhdigAAAAAAAAAAAAA=
```
This creates a transaction but instead of submitting it to stellar, it outputs the serialized binary representation in a format called XDR. This is actually what stellar stores internally. 

## Adding signatures

Stellar requires two signatures for this transaction because we configured this in our previous step. So both alice and bob must add their signatures before we can submit this transaction.

First alice signs the transaction:

```
$ ./cliste -a alice signTx AAAAAGUeetBohq/J9LbvHYve2Am4DNthjdl7xkevBDYYMDJ/AAAAZAAAAMYAAAAGAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAABxTxA11Y4pOjbAv6ZNYFABU77MM0UCjENV+QatyJYFIAAAABRk9PAAAAAABlHnrQaIavyfS27x2L3tgJuAzbYY3Ze8ZHrwQ2GDAyfwFjRXhdigAAAAAAAAAAAAA=
tx hash: zf+uG/7ePiTLuoqLbqgMMyQDq+PlxEsJkVEKq/jEixs=
tx envelope xdr: AAAAAGUeetBohq/J9LbvHYve2Am4DNthjdl7xkevBDYYMDJ/AAAAZAAAAMYAAAAGAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAABxTxA11Y4pOjbAv6ZNYFABU77MM0UCjENV+QatyJYFIAAAABRk9PAAAAAABlHnrQaIavyfS27x2L3tgJuAzbYY3Ze8ZHrwQ2GDAyfwFjRXhdigAAAAAAAAAAAAH763iaAAAAQKZ+gYmDIqv9hUdZdC9+C4bUuX4RWmT8BnCI9wnb35IZ7IZIg5U8NIMvtodGEr4uv3NNB5/tbABEaNtDygihcws=
```

Notice we get back a different XDR. It now includes the signature from Alice. One signature is not enough. So, Alice can now send her signed XDR to Bob via email/slack/etc. who can sign it as well:

```
./cliste -a bob signTx AAAAAGUeetBohq/J9LbvHYve2Am4DNthjdl7xkevBDYYMDJ/AAAAZAAAAMYAAAAGAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAABxTxA11Y4pOjbAv6ZNYFABU77MM0UCjENV+QatyJYFIAAAABRk9PAAAAAABlHnrQaIavyfS27x2L3tgJuAzbYY3Ze8ZHrwQ2GDAyfwFjRXhdigAAAAAAAAAAAAH763iaAAAAQKZ+gYmDIqv9hUdZdC9+C4bUuX4RWmT8BnCI9wnb35IZ7IZIg5U8NIMvtodGEr4uv3NNB5/tbABEaNtDygihcws=
tx hash: zf+uG/7ePiTLuoqLbqgMMyQDq+PlxEsJkVEKq/jEixs=
tx envelope xdr: AAAAAGUeetBohq/J9LbvHYve2Am4DNthjdl7xkevBDYYMDJ/AAAAZAAAAMYAAAAGAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAABxTxA11Y4pOjbAv6ZNYFABU77MM0UCjENV+QatyJYFIAAAABRk9PAAAAAABlHnrQaIavyfS27x2L3tgJuAzbYY3Ze8ZHrwQ2GDAyfwFjRXhdigAAAAAAAAAAAAL763iaAAAAQKZ+gYmDIqv9hUdZdC9+C4bUuX4RWmT8BnCI9wnb35IZ7IZIg5U8NIMvtodGEr4uv3NNB5/tbABEaNtDygihcws2w2UuAAAAQOboMFKz6sOnFPio17cuaOBLrHYN7k/DpFSGAaYVgYKg25YCMqZug2brTkh7LXaubChpFBYJHkF4vN/tUQNSCwM=
```

Bob gets back an even bigger XDR.

## Examining the XDR

Before submitting it, it might be a good idea to check what is inside:

```
$ ./cliste txInfo AAAAAGUeetBohq/J9LbvHYve2Am4DNthjdl7xkevBDYYMDJ/AAAAZAAAAMYAAAAGAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAABxTxA11Y4pOjbAv6ZNYFABU77MM0UCjENV+QatyJYFIAAAABRk9PAAAAAABlHnrQaIavyfS27x2L3tgJuAzbYY3Ze8ZHrwQ2GDAyfwFjRXhdigAAAAAAAAAAAAL763iaAAAAQKZ+gYmDIqv9hUdZdC9+C4bUuX4RWmT8BnCI9wnb35IZ7IZIg5U8NIMvtodGEr4uv3NNB5/tbABEaNtDygihcws2w2UuAAAAQOboMFKz6sOnFPio17cuaOBLrHYN7k/DpFSGAaYVgYKg25YCMqZug2brTkh7LXaubChpFBYJHkF4vN/tUQNSCwM=
850403524614 operations:
source account: GBSR46WQNCDK7SPUW3XR3C663AE3QDG3MGG5S66GI6XQINQYGAZH7CF5
10000000000.0000000 FOO to GADRJ4IDLVMOFE5DNQF7UZGWAUABKO7MYM2FAKGEGVPZA2W4RFQFF6ZQ
Signatures:
pn6BiYMiq/2FR1l0L34LhtS5fhFaZPwGcIj3CdvfkhnshkiDlTw0gy+2h0YSvi6/c00Hn+1sAERo20PKCKFzCw==
5ugwUrPqw6cU+KjXty5o4Eusdg3uT8OkVIYBphWBgqDblgIypm6DZutOSHstdq5sKGkUFgkeQXi83+1RA1ILAw==
```

## Submitting the signed transaction

Now lets submit the transaction:

```
$ ./cliste submitTx AAAAAGUeetBohq/J9LbvHYve2Am4DNthjdl7xkevBDYYMDJ/AAAAZAAAAMYAAAAGAAAAAAAAAAAAAAABAAAAAAAAAAEAAAAABxTxA11Y4pOjbAv6ZNYFABU77MM0UCjENV+QatyJYFIAAAABRk9PAAAAAABlHnrQaIavyfS27x2L3tgJuAzbYY3Ze8ZHrwQ2GDAyfwFjRXhdigAAAAAAAAAAAAL763iaAAAAQKZ+gYmDIqv9hUdZdC9+C4bUuX4RWmT8BnCI9wnb35IZ7IZIg5U8NIMvtodGEr4uv3NNB5/tbABEaNtDygihcws2w2UuAAAAQOboMFKz6sOnFPio17cuaOBLrHYN7k/DpFSGAaYVgYKg25YCMqZug2brTkh7LXaubChpFBYJHkF4vN/tUQNSCwM=
OK
```

And check our distribution account again:

```
$ ./cliste -a distribution balance
accountId: GADRJ4IDLVMOFE5DNQF7UZGWAUABKO7MYM2FAKGEGVPZA2W4RFQFF6ZQ subEntryCount: 1 home domain: null

thresholds: 0 0 0
signers:
	GADRJ4IDLVMOFE5DNQF7UZGWAUABKO7MYM2FAKGEGVPZA2W4RFQFF6ZQ 1
authRequired: false
authRevocable: false

Balances:
FOO (GBSR46WQNCDK7SPUW3XR3C663AE3QDG3MGG5S66GI6XQINQYGAZH7CF5) b:20000000000.0000000 l:922337203685.4775807 - sl: - - bl: -
XLM b:99.9999900 l:- - sl: - - bl:
```

# Alice and Bob can now start using FOO

```
$ ./cliste -a alice trust FOO
$ ./cliste -a bob trust FOO
$ ./cliste -a distribution pay alice 1000000 FOO
$ ./cliste -a alice pay bob 100000 FOO
```

# Other topics

There are way more things you can do with cliste and stellar but this should be enough for a not so gentle introduction. 

You might like using cliste or playing with it to explore Stellar. You can actually do everything via the Stellar Laboratory UI as well. But as you will find it's a lot of clicking and a having cliste around definitely streamlines things. Also, the Stellar Laboratory does not actually work against standalone chains. So, cliste is pretty awesome for that.

## Getting cliste

Follow the instructions on Github. It's a kotlin library project currently. I might move cliste out to its own repository at some point. 

## Using cliste with public or test net

All of the above commands run against a standalone network.

You can actually run against the public network or testnetwork as well with some command line options. Instead of setting these options manually, it is easier to use the included scripts. This is how the clistePublic script works. Note that we use different file names for the assets and accounts as well so we can easily switch.

```
$ cat clistePublic
#! /bin/bash
export CLISTE_ARGS='--stellar-network public --horizon-url https://horizon.stellar.org/ --key-properties public-keys.properties --asset-properties=public-assets.properties'

./cliste $*
```

## Contributing to cliste

I welcome pull requests, issues, feedback, etc.
