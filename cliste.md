# Cliste

As an example of using the library, we developed a small command line interface for stellar.

To build and run it. If you call it without arguments it will print some documentation. 

```
gradle installDist
./cliste help
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