<p align="center">
    <br />
    <img width="256px" src="https://opex.dev/assets/img/opex/opexLogoPlus.svg" alt="Opex" title="Opex">
    <br />
</p>

<p align="center">
Core is a Kotlin based cryptocurrency exchange and matching engine from the <b>OPEX</b> project. This extendable and
microservice project work as a vanilla core for running cryptocurrency exchanges.
</p>

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
- [Environment Variables](#environment-variables)
- [Live Demo](#live-demo)
- [Architecture Overview](#overview)
- [How to Contribute](#how-to-contribute)
- [License](#license)

## <a name="build-and-run"></a>Build and Run

You need to have [Maven](https://maven.apache.org) and [Docker](https://www.docker.com) installed.

1. Clone the repository `git clone https://github.com/opexdev/core.git`
1. Run `cd core`
1. You will need to add the following environment variables to your .env file : [Environment Variables](#environment-variables)
1. Run `mvn clean install` command.
1. Run `docker compose -f .\docker-compose.yml -f .\docker-compose.override.yml -f .\docker-compose.build.yml -f .\docker-compose.local.yml up -d --build`.
1. Run `docker ps` to see if every service is running.


## <a name="environment-variables"></a>Environment Variables

`APP_NAME=Opex-local`

`APP_BASE_URL=localhost:8080`

`PANEL_PASS=admin`

`BACKEND_USER=admin`

`SMTP_PASS=x` -> An SMTP password is a password used to access an email account's 

`OPEX_ADMIN_KEYCLOAK_CLIENT_SECRET=x` -> Ignore this

`API_KEY_CLIENT_SECRET=x` -> (Please do this after starting the project, then rebuild and run the process again.)

1. Go to http://localhost:8083/auth/admin/master/console/#/realms/opex/clients
2. Click on web-app
3. In the Credentials section, click on Regenerate Secret
4. Copy the generated secret and paste it into this section

   
`KEYCLOAK_FRONTEND_URL=http://localhost:8083/auth`

`KEYCLOAK_ADMIN_URL=http://localhost:8083/auth`

`KEYCLOAK_VERIFY_REDIRECT_URL=http://localhost:8080/verify`

`KEYCLOAK_FORGOT_REDIRECT_URL=http://localhost:8080/forgot`

`PREFERENCES=preferences.yml` -> For the initialization

`WHITELIST_REGISTER_ENABLED=true`

`WHITELIST_LOGIN_ENABLED=true`

`WALLET_BACKUP_ENABLED=false`

`TAG=debug`

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
