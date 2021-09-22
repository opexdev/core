<div align="center" style="border-radius:25px" >
  <img width="256px" src="https://opex.dev/images/logo/opex.png" alt="Opex" title="Opex">
</div>

# Opex Core

<p align="center">
  <a href="https://github.com/opexdev/Back-end/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Opex is released under the MIT license." />
  </a>
  <a href="https://opex.dev/docs/contributing">
    <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg" alt="PRs welcome!" />
  </a>
    <a href="https://github.com/opexdev/Back-end/last-commit">
    <img src="https://img.shields.io/github/last-commit/opexdev/Back-end? style=flat-square" alt="Last commit">
  </a>
  <a href="https://github.com/opexdev/Back-end/issues" target="blank">
	<img src="https://img.shields.io/github/issues/opexdev/Back-end? style=flat-square"/>
</a>
</p>

 **OPEX** Core is a Kotlin based cryptocurrency exchange and matching engine from the OPEX project. This extendable and microservice architectured project work as a vanilla core for running cryptocurrency exchanges. 

## Contents

- [Install](#Install)
- [Architecture Overview](#overview)
- [Demo](#demo)
- [Documentation](#documentation)
- [How to Contribute](#how-to-contribute)
- [License](#license)
- [Additional Info](#info)


##  <a name="Install"></a>Install
You need to have [Maven](https://maven.apache.org) and [Docker](https://www.docker.com) installed.

1. Clone this repository or [download the latest zip](https://github.com/opexdev/Back-end).
2. Build each module using `mvn clean install` command.
3. Change directory to `./Deployment` and build docker containers using `docker-compose build`.
4. In `./Deployment` directory, run docker containers which you've built in previous step by using `docker-compose up -d` and wait for modules to be up and running.
5. You can make sure each module is running correctly by typing `http://localhost:8500` to your browser and check module health.
6. You can also make sure middlewares (kafka, consule, etc) are running correctly by using `docker ps`.

## <a name="overview"></a>Architecture Overview

<div align="center" style="border-radius:25px" >
  <img width="800px" src="https://opex.dev/images/overview.jpg" alt="Opex" title="Opex">
</div>

##  <a name="demo"></a>Demo

Check out Opex [demo][WebDemo].

[WebDemo]: https://opex.dev/demo

##  <a name="documentation"></a>Documentation

The full documentation for Opex can be found on our [website][docs].

[docs]: https://opex.dev

## <a name="how-to-contribute"></a>How to Contribute

 We want to make contributing to this project as easy and transparent as possible, and we are grateful to the developer for contributing bug fixes and improvements. Read our contribution docutmentation [here][contribute].

[contribute]: https://opex.dev

## <a name="license"></a>License

Opex is MIT licensed, as found in the [LICENSE][l] file.

[l]: https://github.com/opexdev/Back-end/blob/main/LICENSE

## <a name="info"></a>Additional info

For any other questions, feel free to contact us at [hi@opex.dev](hi@opex.dev).

