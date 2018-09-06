# Cliste

As an example of using the library, we developed a small command line interface for stellar.

To build and run it. If you call it without arguments it will print some documentation. 

```
gradle installDist
./cliste help
```

# Example use

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

usage: cliste [-h] [-k SIGN_KEY] [-v] [-u HORIZON_URL]
              [--asset-properties ASSET_PROPERTIES]
              [--key-properties KEY_PROPERTIES] [COMMAND-NAME] [COMMAND-ARGS]...

optional arguments:
  -h, --help                            show this help message and exit

  -k SIGN_KEY, --sign-key SIGN_KEY      Secret key of the account signing the
                                        transaction. Required for any commands
                                        that do transactions.
                                        
                                        Defaults to the value of the
                                        ST_SECRET_KEY environment variable.

  -v, --verbose                         Verbose output

  -u HORIZON_URL,                       URL for horizon. Defaults to to the
  --horizon-url HORIZON_URL             value of the ST_HORIZON_URL
                                        environment variable or
                                        https://horizon-testnet.stellar.org if
                                        that is empty

  --asset-properties ASSET_PROPERTIES   Properties file with assets

  --key-properties KEY_PROPERTIES       Properties file with named public or
                                        private keys


positional arguments:
  COMMAND-NAME                          command

  COMMAND-ARGS                          command plus command specifics.



Commands:
BALANCE

Shows the account balance of the specified public key.

usage: cliste balance [-h]

optional arguments:
  -h,      show this help message and exit
  --help


OFFERS

usage: cliste offers [-h] [-l LIMIT]

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
  RECEIVER     Receiver account key (public or secret) or key name in
               keys.properties

  AMOUNT       Amount you are paying

  ASSET-CODE   Asset that you are paying with

  MEMO         Optional text memo


TRUST

Trust an asset

usage: cliste trust [-h] ASSET-CODE AMOUNT

optional arguments:
  -h, --help   show this help message and exit


positional arguments:
  ASSET-CODE   Asset that you want to trust. Must be defined in
               assets.properties

  AMOUNT       Amount you trust the asset with


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

- `CLISTE_OPTS` any jvm arguments to configure heap, garbage collection, etc. You should not need this normally.
- `CLISTE_ARGS` default arguments you want to pass to cliste (e.g. your signing key `-k MYKEY`)

Additionally, cliste uses two properties files that you can manage with cliste commands:

- `keys.properties`: a map of key alias to key. You can use either public or private key here. For any argument that takes a key in cliste you can also use the alias. When you do a `cliste createAccount` it will get saved here. You can also use `cliste defineKey` and `cliste listKeys`
- `assets.properties`: a map of asset code to issueing accountId. Use `cliste defineAsset` and `cliste listAssets` to manage
```

# Security

Keys are currently stored in plain text properties files. Obviously not ideal for valuable keys. This tool is intended for use with the standalone or testnet. Be careful where you put your keys!