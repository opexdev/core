<h1 align="center">
    <img width="256px" src="https://demo.opex.dev/static/media/opexLogoPlus.2858c980.svg" alt="Opex" title="Opex">
</h1>

Core is a Kotlin based cryptocurrency exchange and matching engine from the **OPEX** project. This extendable and
microservice project work as a vanilla core for running cryptocurrency exchanges.

<p align="center">
    <a href="https://github.com/opexdev/core/blob/main/LICENSE" target="_blank">
        <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Opex is released under the MIT license." />
    </a>
    <a>
        <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg" alt="PRs welcome!" />
    </a>
    <a href="https://github.com/opexdev/core/last-commit" target="_blank">
        <img src="https://img.shields.io/github/last-commit/opexdev/core? style=flat-square" alt="Last commit">
    </a>
    <a href="https://github.com/opexdev/core/issues" target="_blank">
        <img src="https://img.shields.io/github/issues/opexdev/core? style=flat-square"/>
    </a>
    <a href="https://demo.opex.dev" target="_blank">
        <img src="https://img.shields.io/website?url=https%3A%2F%2Fdemo.opex.dev&logo=react&label=demo.opex.dev" style=flat-square/>
    </a>
</p>

## Contents

- [Build and Run](#build-and-run)
- [Live Demo](#live-demo)
- [Architecture Overview](#overview)
- [How to Contribute](#how-to-contribute)
- [License](#license)

## <a name="build-and-run"></a>Build and Run

You need to have [Maven](https://maven.apache.org) and [Docker](https://www.docker.com) installed.

1. Clone the repository `git clone https://github.com/opexdev/core.git`
1. Run `cd core`
1. Run `mvn clean install` command.
1. Run `docker-compose up --build`.
1. Run `docker ps` to see if every service is running.

## <a name="live-demo"></a>Live Demo

Deployed at [demo.opex.dev](https://demo.opex.dev).

## <a name="overview"></a>Architecture Overview

```mermaid
    graph LR
        USER_MANAGMENT(User Management)
        KAFKA(Kafka)
        ZOOKEEPER(Zookeeper)
        REDIS[(Redis)]
        ACCOUNTANT_POSTGRESQL[(PSQL)]
        REFERRAL_POSTGRESQL[(PSQL)]
        USER_MANAGMENT_POSTGRESQL[(PSQL)]
        WALLET_POSTGRESQL[(PSQL)]
        BC_GATEWAY_POSTGRESQL[(PSQL)]
        EVENTLOG_POSTGRESQL[(PSQL)]
        ACCOUNTANT(Accountant)
        API(API)
        WALLET(Wallet)
        MATCHING_ENGINE(Matching Engine)
        MATCHING_GATEWAY(Matching Gateway)
        REFERRAL(Referral)
        STORAGE(Storage)
        BC_GATEWAY(Blockchain Gateway)
        WEBSOCKET(Websocket)
        ADMIN(Admin)
        CAPTCHA(Captcha)
        EVENTLOG(Event Log)
                
        API-->MATCHING_GATEWAY
        API-->WALLET
        API-->REFERRAL
        API-->STORAGE
        API-->BC_GATEWAY
        API-->ACCOUNTANT
        
        MATCHING_ENGINE-->REDIS
        USER_MANAGMENT-->USER_MANAGMENT_POSTGRESQL
        BC_GATEWAY-->BC_GATEWAY_POSTGRESQL
        REFERRAL-->REFERRAL_POSTGRESQL
        WALLET-->WALLET_POSTGRESQL
        ACCOUNTANT-->ACCOUNTANT_POSTGRESQL
        EVENTLOG-->EVENTLOG_POSTGRESQL
        
        subgraph MESSAGING
            KAFKA
            ZOOKEEPER
        end
        
        subgraph MATCHING DOMAIN
            MATCHING_GATEWAY-->MATCHING_ENGINE
        end
        
        subgraph ACCOUNTANT DOMAIN
            ACCOUNTANT-->WALLET
            REFERRAL-->WALLET
        end
        
        subgraph DATA STORE
            BC_GATEWAY_POSTGRESQL
            REFERRAL_POSTGRESQL
            ACCOUNTANT_POSTGRESQL
            WALLET_POSTGRESQL
            USER_MANAGMENT_POSTGRESQL
            EVENTLOG_POSTGRESQL
            REDIS
        end
```

## <a name="how-to-contribute"></a>How to Contribute

We want to make contributing to this project as easy and transparent as possible, and we are grateful to the developer
for contributing bug fixes and improvements.

## <a name="license"></a>License

OPEX is [MIT licensed](https://github.com/opexdev/core/blob/main/LICENSE).
