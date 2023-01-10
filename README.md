## Vega Protocol - Market Maker

This repository contains a very simple market maker that quotes prices on the Vega Protocol Testnet by tracking the underlying spot market on Binance via a Web Socket connection. The strategy implemented by this market maker is extremely naive, though the code is relatively simple and demonstrates the fundamental concepts needed to trade on Vega Protocol via REST and GraphQL APIs.

### Build

The application is built with Java and Maven. To run the application you can use the `start.sh` script in the root directory.

### Dependencies

You will need a Vega Wallet installed to operate this software: https://vega.xyz/wallet. You also need to define your own environment variables (see `.env.sample`).
