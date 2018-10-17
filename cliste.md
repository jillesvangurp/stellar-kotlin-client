# Cliste

As an example of using the library, we developed a small command line interface for stellar.

To build and run it. If you call it without arguments it will print some documentation. 

```
gradle installDist
./cliste help
```

# Introduction to stellar using cliste

[IntroductionToStellarUsingCliste](IntroductionToStellarUsingCliste.md)

# Documentation

```
$./cliste help
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
