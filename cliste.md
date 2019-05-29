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

[command line documentation](cliste_help.md)

# Security

Keys are currently stored in plain text properties files. Obviously not ideal for valuable keys. This tool is intended for use with the standalone or testnet. Be careful where you put your keys!
