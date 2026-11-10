
# MongoCamp 

![](docs/public/logo_with_text_right.png)

![Docker Pulls](https://img.shields.io/docker/pulls/mongocamp/mongocamp-server) ![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/mongocamp/mongocamp-server?sort=semver) ![Docker Image Version (latest by date)](https://img.shields.io/docker/v/mongocamp/mongocamp-server?sort=semver)![Maven Central](https://img.shields.io/maven-central/v/dev.mongocamp/mongocamp-server_2.13)

## Introduction

Universal REST Handling for MongoDB ....


### Contributing
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/MongoCamp/mongocamp-server)

If you find this library helpfull, so you can see here how you can help:
- Send a pull request with your features and bug fixes
- Help users resolve their [issues](https://github.com/mongocamp/mongocamp-server/issues).

#### Development
Requirements to build and test the project:
- **GraalVM for JDK 25** (for example `cs java --jvm graalvm-java25:25.0.2`). A plain JDK can not compile the project: the library contains the GraalVM native image substitutions (`com.oracle.svm`) and `native-image` builds the CLI and the server binary.
- **sbt 2** (the version is pinned in `project/build.properties`) and **Scala 3** (`scalaVersion` in `build.sbt`).
- **Docker**, the tests start MongoDB with Testcontainers.
- **Node.js LTS** (24 at the moment, the documentation tooling needs at least 20.19 or 22.12) and **pnpm** (version pinned in `package.json`) for the documentation: `pnpm install` and `pnpm docs:dev`.

```shell
sbt clean test                                   # sbt 2 caches task results, use clean to run the tests again
sbt publishLocal                                 # publishes the _3 artifacts to ~/.ivy2/local
sbt mongocamp-cli/GraalVMNativeImage/packageBin  # native CLI, the output is below target/out/jvm/<scala version>/mongocamp-cli/
```

Good to know:
- The native image metadata in `META-INF/native-image` (reflection and resource configuration) is partly maintained by hand. A dependency update can break the native server without any failing test, so after updates build the native image (`mongocamp-cli prepare native`) and start it once.
- Plugins have to be built with Scala 3, see [Plugin Development](https://server.mongocamp.dev/plugins/development/).


### Issues
Feel free to report your issues to Github's issue tracker. Please use one of the templates for reporting issues. [report your issue](https://github.com/mongocamp/mongocamp-server/issues/new/choose)

## Documentation
[Documentation](https://server.mongocamp.dev/) is still on progress.

## ShowCase
[ShowCase](https://showcase.mongocamp.dev/docs/) is a started is an running Instance of MongoCamp-Server with installed [Sample-Plugin](https://github.com/MongoCamp/mongocamp-sample-plugin). All Data are reverted every 30 minutes. 

### Admin User
User: admin
Password: admin

### Default User
User: test
Password: test

## Licence
[Apache 2 License.](https://github.com/mongocamp/mongocamp-server/blob/master/LICENSE)

## Usage
You can use [Docker Compose](https://server.mongocamp.dev/guide/getting-started.html) or directly use directly docker run command.
```shell
docker run --publish 8080:8080 -e CONNECTION_HOST=your.mongo.db.host -e CONNECTION_DATABASE=test -e CONNECTION_USERNAME=mongodbUser -e CONNECTION_PASSWORD=mongodbPwd mongocamp/mongocamp-server:latest
```
