# Cliste

As an example of using the library, we developed a small command line interface for stellar.

To build and run it. If you call it without arguments it will print some documentation. 

```
gradle installDist
./cliste help
```

# Introduction to stellar using cliste

[IntroductionToStellarUsingCliste](IntroductionToStellarUsingCliste.md)

```
$ ./cliste createAccount origin 10000
12:54:05.866 [main] INFO io.inbot.kotlinstellar.KotlinStellarWrapper - using standalone network
12:54:10.483 [main] INFO org.stellar.sdk.KotlinExtensions - 9 7b9883d947bcf6f9b7440e0da19b814375a6e48e47cca70ad6df4140ce94090a success:true fee:100 CREATE_ACCOUNT
created account with secret key SDAYZTHTTLMLLY2F3DTSJ75CDQMZ7GF5RUXQEY4UOP2SCFW26Z2PFOSX

$ ./cliste createAccount -k origin issuer 100
12:54:25.419 [main] INFO org.stellar.sdk.KotlinExtensions - 12 c2cba0628eb963ff380ef5d287d7042b74f6fa884d9d94b818545cdc00f9bb29 success:true fee:100 CREATE_ACCOUNT
created account with secret key SABA7F6PYZKB35QX2K5JKA7HQ5UYL5LDAEEBOJN57VMUYIQVDA647BDD

$ ./cliste createAccount -k origin distributor 100
12:54:40.399 [main] INFO org.stellar.sdk.KotlinExtensions - 15 1294811253f523381e48caa6efddf1df71b1b323e9ae97fe5d4859c484d0cc15 success:true fee:100 CREATE_ACCOUNT
created account with secret key SBE4JHLLS2ZVKHJATHXDS276LUZGT6ORD2BZK37F3VBXMARSCHG7Q7EG

$ ./cliste defineAsset issuer INTT
$ ./cliste -k distributor trust INTT 100000
12:56:00.306 [main] INFO org.stellar.sdk.KotlinExtensions - 31 7017cfcd5d2ca913b1a0ef56b13bbc7d9debfa0f435ebb1abc4341cf3c8a4a4c success:true fee:100 CHANGE_TRUST

$ ./cliste -k issuer help pay
PAY

Pay an amount to another account

usage: cliste pay [-h] RECEIVER AMOUNT ASSET-CODE [MEMO]

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  RECEIVER     Receiver account key (public or secret) or key name in
               keys.properties

  AMOUNT       Amount you are paying

  ASSET-CODE   Asset that you are paying with

  MEMO         Optional text memo


$ ./cliste -k issuer pay distributor 100 INTT
12:56:40.265 [main] INFO org.stellar.sdk.KotlinExtensions - 39 1216560fd638a52b64d805b0d5de7147a4023661bd1aecacd367e4b4b58079b6 success:true fee:100 PAYMENT

$ ./cliste -k distributor balance
accountId: GD2IWOWOR54P54S32QCTPA4YYJFZVGT7ELF4DY5NAKVXWOVR567EMBHC subEntryCount: 1 home domain: null

Balances:
balance INTT 100.0000000 100000.0000000
balance XLM 99.9999900 null
```

# Documentation

```
CliSte -  the Commnand Line Interface for Stellar

usage: cliste [-h] [-a ACCOUNT_KEY] [-s SIGNING_KEY]... [-v] [-u HORIZON_URL] [--asset-properties ASSET_PROPERTIES]
              [--key-properties KEY_PROPERTIES] [--stand-alone-network-passphrase STAND_ALONE_NETWORK_PASSPHRASE]
              [--stellar-network STELLAR_NETWORK] [COMMAND-NAME]

optional arguments:
  -h, --help                                                        show this help message and exit

  -a ACCOUNT_KEY, --account-key ACCOUNT_KEY                         Account key of the account for the transaction.
                                                                    Required for any commands that do transactions on
                                                                    accounts.
                                                                    
                                                                    Defaults to the value of the ST_ACCOUNT_KEY
                                                                    environment variable.

  -s SIGNING_KEY, --signing-key SIGNING_KEY                         Signing key

  -v, --verbose                                                     Verbose output

  -u HORIZON_URL, --horizon-url HORIZON_URL                         URL for horizon. Defaults to to the value of the
                                                                    ST_HORIZON_URL environment variable or
                                                                    http://localhost:8000 if that is empty. If you are
                                                                    planning to run lots of commands against stellar,
                                                                    you should consider setting up your own horizon
                                                                    server to avoid rate limiting on the public
                                                                    endpoints.

  --asset-properties ASSET_PROPERTIES                               Properties file with assets

  --key-properties KEY_PROPERTIES                                   Properties file with named public or private keys

  --stand-alone-network-passphrase STAND_ALONE_NETWORK_PASSPHRASE   network password. You can leave this blank for
                                                                    testnet or public. It defaults to the passphrase
                                                                    for the standalone network you get with the
                                                                    quickstart docker image.

  --stellar-network STELLAR_NETWORK                                 


positional arguments:
  COMMAND-NAME                                                      command



Commands:
BALANCE

Shows the account balance of the specified public key.

usage: cliste balance [-h]

optional arguments:
  -h,      show this help message and exit
  --help


LISTOFFERS

usage: cliste listOffers [-h] [-l LIMIT]

optional arguments:
  -h, --help      show this help message and exit

  -l LIMIT,       Limit
  --limit LIMIT


DEFINEASSET

usage: cliste defineAsset [-h] ISSUER ASSET-CODE

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  ISSUER       public key of the issuer

  ASSET-CODE   4 or 12 letter asset code


LISTASSETS

List the defined assets

usage: cliste listAssets [-h]

optional arguments:
  -h,      show this help message and exit
  --help


DEFINEKEY

usage: cliste defineKey [-h] NAME KEY

optional arguments:
  -h,      show this help message and exit
  --help


positional arguments:
  NAME     name of the key

  KEY      key


LISTKEYS

List the defined keys

usage: cliste listKeys [-h]

optional arguments:
  -h,      show this help message and exit
  --help


CREATEACCOUNT

Create a new account

usage: cliste createAccount [-h] [NAME] [AMOUNT]

optional arguments:
  -h,      show this help message and exit
  --help


positional arguments:
  NAME     name under which to store the new key, defaults to key-<timestamp>

  AMOUNT   Amount XML to be transferred to the new account (default 20)


PAY

Pay an amount to another account

usage: cliste pay [-h] RECEIVER AMOUNT ASSET-CODE [MEMO]

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  RECEIVER     Receiver account key (public or secret) or key name in keys.properties

  AMOUNT       Amount you are paying

  ASSET-CODE   Asset that you are paying with

  MEMO         Optional text memo


PREPAREPAYMENTTX

Prepare an XDR transaction for a payment. Prints the
 XDR of the transaction envelope so you can send it to the signees.

usage: cliste preparePaymentTX [-h] RECEIVER AMOUNT ASSET-CODE [MEMO]

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  RECEIVER     Receiver account key (public or secret) or key name in keys.properties

  AMOUNT       Amount you are paying

  ASSET-CODE   Asset that you are paying with

  MEMO         Optional text memo


TXINFO

Show information about an XDR transaction envelope.

usage: cliste txInfo [-h] RECEIVER AMOUNT ASSET-CODE [MEMO]

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  RECEIVER     Receiver account key (public or secret) or key name in keys.properties

  AMOUNT       Amount you are paying

  ASSET-CODE   Asset that you are paying with

  MEMO         Optional text memo


SIGNTX

Add a signature to a transaction envelope in XDR form.

usage: cliste signTx [-h] RECEIVER AMOUNT ASSET-CODE [MEMO]

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  RECEIVER     Receiver account key (public or secret) or key name in keys.properties

  AMOUNT       Amount you are paying

  ASSET-CODE   Asset that you are paying with

  MEMO         Optional text memo


SUBMITTX

Submit a transaction envelope in XDR form. You should add signatures first using signTx.

usage: cliste submitTx [-h] RECEIVER AMOUNT ASSET-CODE [MEMO]

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  RECEIVER     Receiver account key (public or secret) or key name in keys.properties

  AMOUNT       Amount you are paying

  ASSET-CODE   Asset that you are paying with

  MEMO         Optional text memo


LISTTRADES

List trades

usage: cliste listTrades [-h] BASE-ASSET COUNTER-ASSET

optional arguments:
  -h, --help      show this help message and exit


positional arguments:
  BASE-ASSET      Base asset

  COUNTER-ASSET   Counter asset


LISTTRADEAGGS

List trade aggregations

usage: cliste listTradeAggs [-h] [--from FROM] [--to TO] [-r RESOLUTION] BASE-ASSET COUNTER-ASSET

optional arguments:
  -h, --help                show this help message and exit

  --from FROM               From time in ms after epoch. Default to now-24h

  --to TO                   to time in ms after epoch. Default now

  -r RESOLUTION,            resolution. One of T1_MINUTES, T5_MINUTES, T15_MINUTES, T1_HOURS, T1_DAYS, T1_WEEKS
  --resolution RESOLUTION


positional arguments:
  BASE-ASSET                Base asset

  COUNTER-ASSET             Counter asset


TRUST

Trust an asset

usage: cliste trust [-h] ASSET-CODE [AMOUNT]

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  ASSET-CODE   Asset that you want to trust. Must be defined in assets.properties

  AMOUNT       Amount you trust the asset with


SETOPTIONS

Set options on an account

usage: cliste setOptions [-h] [--low-threshold LOW_THRESHOLD] [--medium-threshold MEDIUM_THRESHOLD]
                         [--high-threshold HIGH_THRESHOLD] [--master-key-weight MASTER_KEY_WEIGHT]
                         [--signer-weight SIGNER_WEIGHT] [--signer-key SIGNER_KEY] [--home-domain HOME_DOMAIN]

optional arguments:
  -h, --help                              show this help message and exit

  --low-threshold LOW_THRESHOLD           

  --medium-threshold MEDIUM_THRESHOLD     

  --high-threshold HIGH_THRESHOLD         

  --master-key-weight MASTER_KEY_WEIGHT   

  --signer-weight SIGNER_WEIGHT           

  --signer-key SIGNER_KEY                 

  --home-domain HOME_DOMAIN               


LISTASSETSONSTELLAR

lists all assets on stellar

usage: cliste listAssetsOnStellar [-h]

optional arguments:
  -h,      show this help message and exit
  --help


HELP

Show help for a specific command

usage: cliste help [-h] [COMMAND]

optional arguments:
  -h,       show this help message and exit
  --help


positional arguments:
  COMMAND   name of the command


Configuring CliSte

You can configure cliste using two environment variables

- `CLISTE_OPTS` any jvm arguments to
configure heap, garbage collection, etc. You should not need this normally.
- `CLISTE_ARGS` default arguments you want
to pass to cliste (e.g. your signing key `-k MYKEY`)

Additionally, cliste uses two properties files that you can manage
with cliste commands:

- `keys.properties`: a map of key alias to key. You can use either public or private key here.
For any argument that takes a key in cliste you can also use the alias. When you do a `cliste createAccount` it will get
saved here. You can also use `cliste defineKey` and `cliste listKeys`
- `assets.properties`: a map of asset code to
issueing accountId. Use `cliste defineAsset` and `cliste listAssets` to manage
```

# Security

Keys are currently stored in plain text properties files. Obviously not ideal for valuable keys. This tool is intended for use with the standalone or testnet. Be careful where you put your keys!
